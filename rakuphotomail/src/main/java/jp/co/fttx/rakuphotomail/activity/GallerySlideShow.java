/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.Attachments;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.MessageInfo;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideCheck;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;
import jp.co.fttx.rakuphotomail.service.AttachmentSyncReceiver;
import jp.co.fttx.rakuphotomail.service.AttachmentSyncService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideShow extends RakuPhotoActivity implements View.OnClickListener {

    /**
     * Intent get/put account uuid
     */
    private static final String EXTRA_ACCOUNT = "account";
    /**
     * Intent get/put folder name
     */
    private static final String EXTRA_FOLDER = "folder";
    /**
     * Intent get/put folder name
     */
    private static final String EXTRA_UID = "uid";
    /**
     * Bundle put/get Mail Subject
     */
    private static final String MESSAGE_SUBJECT = "subject";
    /**
     * Bundle put/get Mail Subject
     */
    private static final String MESSAGE_DATE = "date";
    /**
     * Bundle put/get Mail Subject
     */
    private static final String MESSAGE_UID_LIST = "uidList";
    /**
     * Bundle put/get Mail Subject
     */
    private static final String MESSAGE_ANSWERED = "answered";
    /**
     * R.id.gallery_attachment_picture_default
     */
    private static final int ID_GALLERY_ATTACHMENT_PICTURE_DEFAULT = R.id.gallery_attachment_picture_default;
    /**
     * R.id.gallery_attachment_picture_even
     */
    private static final int ID_GALLERY_ATTACHMENT_PICTURE_EVEN = R.id.gallery_attachment_picture_even;
    /**
     * R.id.gallery_attachment_picture_odd
     */
    private static final int ID_GALLERY_ATTACHMENT_PICTURE_ODD = R.id.gallery_attachment_picture_odd;

    /**
     * account
     */
    private Account mAccount;
    /**
     * folder name
     */
    private String mFolder;
    /**
     * start UID
     */
    private String mStartUid;
    /**
     * context
     */
    private Context mContext;
    /**
     * view info layout(subject,date...)
     */
    private LinearLayout mInfo;
    /**
     * view subject
     */
    private TextView mSubject;
    /**
     * view slide image default
     */
    private ImageView mImageViewDefault;
    /**
     * view slide image(even)
     */
    private ImageView mImageViewEven;
    /**
     * view slide image(odd)
     */
    private ImageView mImageViewOdd;
    /**
     * service
     */
    private AttachmentSyncService mSyncService;
    /**
     * isBound
     */
    private boolean mIsBound = false;
    /**
     * receiver
     */
    private AttachmentSyncReceiver mAttachmentReceiver = new AttachmentSyncReceiver();
    /**
     * mesage uid's list
     */
//    private CopyOnWriteArrayList<String> mUidList = new CopyOnWriteArrayList<String>();
    private ArrayList<String> mUidList = new ArrayList<String>();
    /**
     * mesage uid's list
     */
    private ArrayList<String> mAllUidList = new ArrayList<String>();
    /**
     * Uid List is Repeat?
     */
    private boolean mIsRepeatUidList = true;
    /**
     * Handler
     */
    private Handler mSlideHandler;
    /**
     * Thread SlideShow
     */
    private Thread mSlideShowThread;
    /**
     * Handler
     */
    private Handler mProgressHandler;
    /**
     *
     */
    private ProgressDialog mProgressDialog;
    /**
     *
     */
    private String mDispUid = null;
    /**
     * mail receive date
     */
    private TextView mDate;
    /**
     * mail sent flag
     */
    private TextView mAnswered;
    /**
     * Display Receive Date
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd h:mm a";
    /**
     * polling timer
     */
    private Timer mTimer;

    /**
     * @param context context
     * @param account account info
     * @param folder  receive folder name
     * @param uid     message uid
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void actionSlideShow(Context context, Account account, String folder, String uid) {
        Intent intent = new Intent(context, GallerySlideShow.class);
        if (account != null) {
            intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
        }
        if (folder != null) {
            intent.putExtra(EXTRA_FOLDER, folder);
        }
        if (uid != null) {
            intent.putExtra(EXTRA_UID, uid);
        } else {
            intent.putExtra(EXTRA_UID, "");
        }
        context.startActivity(intent);
    }

    /**
     * @param savedInstanceState saved
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show);
        mProgressDialog = new ProgressDialog(this);
        setUpProgressDialog(mProgressDialog);
        onNewIntent(getIntent());
        doBindService();
        setupViews();
        doAllFolderSync();
        createUidList(null, mAccount.getMessageLimitCountFromDb());
        mAllUidList = getUidList(null, 0);
        setupSlideShowThread();

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 同期処理で新着メールを見つけられた場合
                String newMailUid = MessageSync.syncMailboxForCheckNewMail(mAccount, mFolder, mAccount.getMessageLimitCountFromRemote());
                doOutBoxFolderSync();
                doSentFolderSync();
                if (null != newMailUid && !"".equals(newMailUid) && isSlide(newMailUid)) {
                    doUnbindService();
                    mIsRepeatUidList = false;
                    try {
                        if (mSlideShowThread.isAlive()) {
                            mSlideShowThread.join();
                        } else {
                            mDispUid = newMailUid;
                        }
                    } catch (InterruptedException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
                    }
                    GalleryNewMail.actionHandle(mContext, mAccount, mFolder, newMailUid, mDispUid);
                    mAllUidList.clear();
                    mAllUidList = getUidList(null, 0);
                    finish();
                }

                // サーバーとつながってる状態で新着メールがローカル取り込み完了している場合
                ArrayList<String> newUidList = getUidList(null, mAccount.getMessageLimitCountFromDb());
                for (String uid : newUidList) {
                    if (!mAllUidList.contains(uid) && isSlide(uid)) {
                        doUnbindService();
                        mIsRepeatUidList = false;
                        try {
                            if (mSlideShowThread.isAlive()) {
                                mSlideShowThread.join();
                            } else {
                                mDispUid = uid;
                            }
                        } catch (InterruptedException e) {
                            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
                        }

                        GalleryNewMail.actionHandle(mContext, mAccount, mFolder, uid, mDispUid);
                        mAllUidList.clear();
                        mAllUidList = getUidList(null, 0);
                        finish();
                    }
                }
            }
        }, 180000L, 180000L);

    }

    /**
     * 添付ファイル中、1つでも表示可能なものを持っているならOK.
     *
     * @param uid unique id
     * @return boolean slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private boolean isSlide(String uid) {
        ArrayList<Attachments> attachmentsList = SlideMessage.getAttachmentList(mAccount, mFolder, uid);
        for (Attachments attachments : attachmentsList) {
            if (SlideCheck.isSlide(attachments)) {
                return true;
            }
        }
        return false;
    }

    private void doAllFolderSync() {
        doInBoxFolderSync();
        doOutBoxFolderSync();
        doSentFolderSync();
    }

    private void doInBoxFolderSync() {
        MessageSync.syncMailbox(mAccount, mAccount.getInboxFolderName(), mAccount.getMessageLimitCountFromRemote());
    }

    private void doOutBoxFolderSync() {
        MessageSync.syncMailboxForCheckNewMail(mAccount, mAccount.getOutboxFolderName(), 0);
    }

    private void doSentFolderSync() {
        MessageSync.syncMailboxForCheckNewMail(mAccount, mAccount.getSentFolderName(), 0);
    }

    private void setUpProgressDialog(ProgressDialog progressDialog) {
        if (!progressDialog.isShowing()) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("しばらくお待ちください");
            progressDialog.setMessage("サーバーと同期し、スライドショー用の画像をダウンロード中です。\n完了次第、スライドショーを開始します。");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    private void dismissProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * view setup
     */
    private void setupViews() {
        mInfo = (LinearLayout) findViewById(R.id.gallery_info);
        setImageViewDefault();
        setImageViewEven();
        setImageViewOdd();
        mSubject = (TextView) findViewById(R.id.gallery_subject);
        mSlideHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                setVisibilityImageView().setImageBitmap((Bitmap) msg.obj);
                Bundle bundle = msg.getData();
                mSubject.setText(bundle.get(MESSAGE_SUBJECT).toString());
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                mDate.setText(sdf.format(bundle.get(MESSAGE_DATE)));
                if ((Boolean) bundle.get(MESSAGE_ANSWERED)) {
                    mAnswered.setVisibility(View.VISIBLE);
                } else {
                    mAnswered.setVisibility(View.GONE);
                }
            }
        };
//        mProgressHandler = new Handler(){
//            public void handleMessage(android.os.Message msg) {
//                mProgressDialog = new ProgressDialog(mContext);
//                setUpProgressDialog(mProgressDialog);
//                for (String uid : mUidList) {
//                    Log.d("asakusa", "loopInfinite clearCacheForAttachmentFileしますか uid:" + uid);
//                    try {
//                        SlideAttachment.clearCacheForAttachmentFile(mAccount, mFolder, uid);
//                    } catch (MessagingException e) {
//                        e.printStackTrace();
//                    }
//                }
//                mSyncService.clearAllDownloadedUid();
//                createUidList(mDispUid, mAccount.getMessageLimitCountFromDb());
//                //TODO ちょうじりあわせ
//                while (is) {
//
//                }
//                dismissProgressDialog(mProgressDialog);
//            }
//        };
        mDate = (TextView) findViewById(R.id.gallery_date);
        mAnswered = (TextView) findViewById(R.id.gallery_sent_flag);
        mAnswered.setVisibility(View.GONE);
    }

    /**
     * view setup
     */
    private void setImageViewDefault() {
        mImageViewDefault = (ImageView) findViewById(ID_GALLERY_ATTACHMENT_PICTURE_DEFAULT);
        if ("".equals(mStartUid)) {
            mImageViewDefault.setVisibility(View.VISIBLE);
            mInfo.setVisibility(View.GONE);
        } else {
            mImageViewDefault.setVisibility(View.GONE);
            mInfo.setVisibility(View.VISIBLE);
        }
    }

    /**
     * view setup
     */
    private void setImageViewEven() {
        mImageViewEven = (ImageView) findViewById(ID_GALLERY_ATTACHMENT_PICTURE_EVEN);
        mImageViewEven.setOnClickListener(this);
        mImageViewEven.setVisibility(View.GONE);
    }

    /**
     * view setup
     */
    private void setImageViewOdd() {
        mImageViewOdd = (ImageView) findViewById(ID_GALLERY_ATTACHMENT_PICTURE_ODD);
        mImageViewOdd.setOnClickListener(this);
        mImageViewOdd.setVisibility(View.GONE);
    }

    /**
     * @param intent intent
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onNewIntent(Intent intent) {
        mAccount = Preferences.getPreferences(this).getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
        mFolder = intent.getStringExtra(EXTRA_FOLDER);
        mStartUid = intent.getStringExtra(EXTRA_UID);
        intent.setClass(mContext, AttachmentSyncService.class);
        setIntent(intent);
    }

    /**
     * UIDリスト作成
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void createUidList(String currentUid, long limitCount) {
        List<MessageInfo> messageInfoList = SlideMessage.getMessageInfoList(mAccount, mFolder, currentUid, limitCount);
        if (null == messageInfoList || 0 == messageInfoList.size()) {
            if (null != currentUid) {
                // currentUidがnullではない場合、DBの一番最後の可能性が有るため、最初から取り直す
                messageInfoList = SlideMessage.getMessageInfoList(mAccount, mFolder, null, limitCount);
                if (null == messageInfoList || 0 == messageInfoList.size()) {
                    Log.w(RakuPhotoMail.LOG_TAG, "現在、サーバー上に受信メールが存在しません");
                    dismissProgressDialog(mProgressDialog);
                    onAlertNoMessage();
                    return;
                }
            } else {
                Log.w(RakuPhotoMail.LOG_TAG, "現在、サーバー上に受信メールが存在しません");
                dismissProgressDialog(mProgressDialog);
                onAlertNoMessage();
                return;
            }
        }
        mUidList.clear();
        for (MessageInfo messageInfo : messageInfoList) {
            String uid = messageInfo.getUid();
            ArrayList<Attachments> attachmentsList = SlideMessage.getAttachmentList(mAccount, mFolder, uid);
            for (Attachments attachments : attachmentsList) {
                //追加対象はスライドする奴のみ（重複UIDは省く）
                if (SlideCheck.isSlide(attachments) && !mUidList.contains(uid)) {
                    mUidList.add(uid);
                }
            }
        }
    }

    private ArrayList<String> getUidList(String currentUid, long limitCount) {
        List<MessageInfo> messageInfoList = SlideMessage.getMessageInfoList(mAccount, mFolder, currentUid, limitCount);
        ArrayList<String> uidList = new ArrayList<String>();
        for (MessageInfo messageInfo : messageInfoList) {
            String uid = messageInfo.getUid();
            ArrayList<Attachments> attachmentsList = SlideMessage.getAttachmentList(mAccount, mFolder, uid);
            for (Attachments attachments : attachmentsList) {
                //追加対象はスライドする奴のみ（重複UIDは省く）
                if (SlideCheck.isSlide(attachments) && !uidList.contains(uid)) {
                    uidList.add(uid);
                }
            }
        }
        return uidList;
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void doBindService() {
        Log.d("asakusa", "AttachmentSyncService#doBindService");
        if (!mIsBound) {
            mIsBound = bindService(getIntent(), mConnection, Context.BIND_AUTO_CREATE);
            IntentFilter attachmentFilter = new IntentFilter(AttachmentSyncService.ACTION_SLIDE_SHOW);
            registerReceiver(mAttachmentReceiver, attachmentFilter);
        }
    }

    private void doUnbindService() {
        try {
            if (mIsBound) {
                unbindService(mConnection);
                mIsBound = false;
                unregisterReceiver(mAttachmentReceiver);
            }
        } catch (Exception cce) {
            Log.e(RakuPhotoMail.LOG_TAG, "doUnbindService Error:" + cce);
        }
    }

    /**
     * ServiceConnection
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("asakusa", "AttachmentSyncService#mConnection");
            try {
                Log.d("asakusa", "AttachmentSyncService#mConnection mSyncService");
                mSyncService = ((AttachmentSyncService.AttachmentSyncBinder) service).getService();

            } catch (ClassCastException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow ServiceConnection#onServiceConnected Error:" + e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mSyncService = null;
        }
    };

    private void setupSlideShowThread() {
        mSlideShowThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loopInfinite();
                } catch (RakuRakuException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onSlide thread Error:" + e);
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onSlide thread Error:" + e);
                }
            }
        });
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onResume() {
        super.onResume();
        if (null != mUidList && 0 < mUidList.size()) {
            onSlide();
        }
    }

    /**
     * slide.
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void onSlide() {
        mSlideShowThread.start();
    }

    /**
     * loooooooop.
     *
     * @throws RakuRakuException  rakuraku!
     * @throws MessagingException me
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void loopInfinite() throws RakuRakuException, MessagingException {
        while (mIsRepeatUidList) {
            createUidListForReSlide();
            downloadBundle();
            loop();
//            mProgressHandler.sendMessage(new Message());
        }
    }

//    private static boolean is = true;
//    public static void hoge() {
//        //
//        Log.d("asakusa", "hogeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
//        is = false;
//    }

    private void createUidListForReSlide() {
        if (!"".equals(mStartUid)) {
            createUidList(mStartUid, mAccount.getMessageLimitCountFromDb());
            mStartUid = "";
        }
    }

    private void downloadBundle() throws MessagingException {
        for (String uid : mUidList) {
            Log.d("asakusa", "AttachmentSyncService#onDownload 一括DL while before uid:" + uid);
            while (null == mSyncService) {
            }
            Log.d("asakusa", "AttachmentSyncService#onDownload 一括DL while after uid:" + uid);
            mSyncService.onDownload(mAccount, mFolder, uid, AttachmentSyncService.ACTION_SLIDE_SHOW);
        }
    }

    /**
     * uid loop.
     *
     * @throws RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void loop() throws RakuRakuException {
        for (String uid : mUidList) {
            dispSlide(uid);
            if (!mIsRepeatUidList) {
                return;
            }
        }
    }

    /**
     * @param uid mail uid
     * @throws RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(String uid) throws RakuRakuException {
        Log.e("asakusa", "dispSlide uid:" + uid);
        MessageBean messageBean = SlideMessage.getMessage(mAccount, mFolder, uid);
        if (SlideCheck.isDownloadedAttachment(messageBean)) {
            dismissProgressDialog(mProgressDialog);
            dispSlide(messageBean);
        } else {
            try {
                if (null == mSyncService) {
                    Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#loopUid mSyncServiceがnullでした。");
                    return;
                }
                Log.e("asakusa", "dispSlide mSyncService.onDownload 任意DL uid:" + uid);
                mSyncService.onDownload(mAccount, mFolder, uid, AttachmentSyncService.ACTION_SLIDE_SHOW);
            } catch (MessagingException e) {
                Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#loopUid 次のuid(" + uid + ")をDLしますね");
            }
        }
    }

    /**
     * slide show dispppppppp.
     *
     * @param messageBean MessageBean
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(MessageBean messageBean) {
        ArrayList<AttachmentBean> attachmentBeanList = messageBean.getAttachmentBeanList();
        for (AttachmentBean attachmentBean : attachmentBeanList) {
            if (SlideCheck.isSlide(attachmentBean)) {
                Bitmap bitmap = SlideAttachment.getBitmap(mContext, getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
                if (null == bitmap) {
                    return;
                }
                Message msg = setSendMessage(messageBean);
                msg.obj = bitmap;
                mSlideHandler.sendMessage(msg);
                mDispUid = messageBean.getUid();
                // TODO 停止時間はアカウントクラスで保持する方針でいく
                sleepSlide(3500L);
            }
        }
    }

    /**
     * @param messageBean MesasgeBean
     * @return Message(Handler)
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private Message setSendMessage(MessageBean messageBean) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_SUBJECT, messageBean.getSubject());
        bundle.putLong(MESSAGE_DATE, messageBean.getDate());
        bundle.putBoolean(MESSAGE_ANSWERED, messageBean.isFlagAnswered());
        msg.setData(bundle);
        return msg;
    }

    /**
     * @param sleepTime sleep
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void sleepSlide(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#stopSlide error:" + e);
        }
    }

    /**
     * @return imageView
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private ImageView setVisibilityImageView() {
        ImageView imageView;
        if (mImageViewEven.getVisibility() == View.GONE) {
            mImageViewDefault.setVisibility(View.GONE);
            mImageViewEven.setVisibility(View.VISIBLE);
            mImageViewOdd.setVisibility(View.GONE);
            imageView = mImageViewEven;
        } else if (mImageViewOdd.getVisibility() == View.GONE) {
            mImageViewDefault.setVisibility(View.GONE);
            mImageViewEven.setVisibility(View.GONE);
            mImageViewOdd.setVisibility(View.VISIBLE);
            imageView = mImageViewOdd;
        } else {
            mImageViewDefault.setVisibility(View.GONE);
            mImageViewEven.setVisibility(View.VISIBLE);
            mImageViewOdd.setVisibility(View.GONE);
            imageView = mImageViewEven;
        }
        mInfo.setVisibility(View.VISIBLE);
        return imageView;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            doUnbindService();
            mIsRepeatUidList = false;
            mSlideShowThread.join();
        } catch (InterruptedException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onStop Error:" + e);
            finish();
        } catch (ClassCastException cce) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onStop Error:" + cce);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mTimer.cancel();
            mTimer = null;
        } catch (ClassCastException cce) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onDestroy Error:" + cce);
        }
    }

    /**
     * @param outState Bundle
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        outState.putString(EXTRA_FOLDER, mFolder);
        outState.putString(EXTRA_UID, mStartUid);
    }

    /**
     * @param savedInstanceState saved
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAccount = Preferences.getPreferences(this)
                .getAccount(savedInstanceState.getString(EXTRA_ACCOUNT));
        mFolder = savedInstanceState.getString(EXTRA_FOLDER);
        mStartUid = savedInstanceState.getString(EXTRA_UID);
    }

    /**
     * @param v view
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_GALLERY_ATTACHMENT_PICTURE_EVEN:
                try {
                    onSlideStop(mDispUid);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case ID_GALLERY_ATTACHMENT_PICTURE_ODD:
                try {
                    onSlideStop(mDispUid);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Log.w(RakuPhotoMail.LOG_TAG, "onClick is no Action !!!!");
        }
    }

    /**
     * event slide stop
     *
     * @param uid message uid
     * @throws InterruptedException InterruptedException
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void onSlideStop(String uid) throws InterruptedException {
        if (null != mDispUid) {
            try {
                doUnbindService();
                mIsRepeatUidList = false;
                mSlideShowThread.join();
                Log.e("asakusa", "onSlideStop uid:" + uid);
                GallerySlideStop.actionHandle(mContext, mAccount, mFolder, uid);
                finish();
            } catch (InterruptedException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onStop Error:" + e);
                finish();
            }
        }
    }

    private void onAlertNoMessage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("らくフォトメール");
        alertDialogBuilder.setMessage("サーバー上にメールが存在しません。\n終了しますか？\n（注）このまま新着メールの待ち受けも可能です。");
        alertDialogBuilder.setPositiveButton("はい、終了します",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("refs1961", "onAlertNoMessage PositiveButton click");
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton("いいえ、待ち受けします",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("refs1961", "onAlertNoMessage NegativeButton click");
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
