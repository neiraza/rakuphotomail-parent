/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
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
    private static final String MESSAGE_SENDER_NAME = "senderName";
    /**
     * Bundle put/get Mail Subject
     */
    private static final String MESSAGE_DATE = "date";
    /**
     * Bundle put/get Mail Subject
     */
    private static final String MESSAGE_UID = "uid";
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
     * view subject
     */
    private TextView mSubject;
    /**
     * view subject
     */
    private TextView mSenderName;
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
     * mesage uid's list
     */
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
    private Handler mSlideShowHandler = new Handler();
    /**
     * Handler
     */
    private Handler mProgressHandler = new Handler();
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
     * mail sent flag
     */
    private ImageView mAnsweredMark;

    /**
     * Display Receive Date
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd h:mm a";
    /**
     * polling timer
     */
    private Timer mTimer;
    /**
     *
     */
    private boolean isDownloaded = false;
    /**
     *
     */
    private boolean isClear = false;
    /**
     *
     */
    private ArrayList<String> mRemoveList = new ArrayList<String>();


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
        setContentView(R.layout.gallery_slide_show_postcard1);
        mProgressDialog = new ProgressDialog(mContext);
        setUpProgressDialog(mProgressDialog,"Please wait","スライドショー情報をサーバーと同期中です。\n完了次第、スライドショーを開始します。\nしばらくお待ちください。");
        onNewIntent(getIntent());
        setupViews();
        doAllFolderSync();
        createUidList(null, mAccount.getMessageLimitCountFromDb());
        mAllUidList = getUidList(null, 0);
        setupSlideShowThread();

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("newMailCheck", "新着メールをチェックしますね");
                // 同期処理で新着メールを見つけられた場合
                String newMailUid = MessageSync.syncMailboxForCheckNewMail(mAccount, mFolder, mAccount.getMessageLimitCountFromRemote());
                doSentFolderSync();
                if (null != newMailUid && !"".equals(newMailUid) && isSlide(newMailUid)) {
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
        doSentFolderSync();
    }

    private void doInBoxFolderSync() {
        MessageSync.syncMailbox(mAccount, mAccount.getInboxFolderName(), mAccount.getMessageLimitCountFromRemote());
    }

    private void doSentFolderSync() {
        MessageSync.syncMailboxForCheckNewMail(mAccount, mAccount.getSentFolderName(), 0);
    }

    private void setUpProgressDialog(ProgressDialog progressDialog,String title, String message) {
        if (!progressDialog.isShowing()) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
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
        setImageViewDefault();
        setImageViewEven();
        setImageViewOdd();
        mSubject = (TextView) findViewById(R.id.gallery_subject);
        mSenderName = (TextView)findViewById(R.id.gallery_sender_name);
        mSlideHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                setVisibilityImageView().setImageBitmap((Bitmap) msg.obj);
                Bundle bundle = msg.getData();
                mDispUid = bundle.get(MESSAGE_UID).toString();
                mSubject.setText(bundle.get(MESSAGE_SUBJECT).toString());
                mSenderName.setText(bundle.get(MESSAGE_SENDER_NAME).toString());
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                mDate.setText(sdf.format(bundle.get(MESSAGE_DATE)));
                if ((Boolean) bundle.get(MESSAGE_ANSWERED)) {
                    mAnswered.setVisibility(View.VISIBLE);
                    mAnsweredMark.setVisibility(View.VISIBLE);
                } else {
                    mAnswered.setVisibility(View.GONE);
                    mAnsweredMark.setVisibility(View.GONE);
                }
            }
        };
        mDate = (TextView) findViewById(R.id.gallery_date);
        mAnswered = (TextView) findViewById(R.id.gallery_sent_flag);
        mAnsweredMark = (ImageView)findViewById(R.id.gallery_sent_flag_mark);
        mAnswered.setVisibility(View.GONE);
        mAnsweredMark.setVisibility(View.GONE);
    }

    /**
     * view setup
     */
    private void setImageViewDefault() {
        mImageViewDefault = (ImageView) findViewById(ID_GALLERY_ATTACHMENT_PICTURE_DEFAULT);
        if ("".equals(mStartUid)) {
            mImageViewDefault.setVisibility(View.VISIBLE);
//            mInfo.setVisibility(View.GONE);
        } else {
            mImageViewDefault.setVisibility(View.GONE);
//            mInfo.setVisibility(View.VISIBLE);
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
        try {
            while (mIsRepeatUidList) {
                if (!"".equals(mStartUid)) {
                    createUidList(mStartUid, mAccount.getMessageLimitCountFromDb());
                    mStartUid = "";
                }
                dismissProgressDialog(mProgressDialog);

                mProgressHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog = new ProgressDialog(mContext);
                        setUpProgressDialog(mProgressDialog,"Please wait","スライドショー情報をサーバーと同期中です。\nしばらくお待ちください。  ");
                    }
                });
                for (int i = 0; i < mUidList.size(); i++) {
                    SlideAttachment.downloadAttachment(mAccount, mFolder, mUidList.get(i));
                }
                dismissProgressDialog(mProgressDialog);

                //slide show
                loop();

                ArrayList<String> downloadedList = SlideMessage.getMessageUidRemoveTarget(mAccount);
                if (downloadedList.size() > mAccount.getAttachmentCacheLimitCount()) {
                    mProgressHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog = new ProgressDialog(mContext);
                            setUpProgressDialog(mProgressDialog,"Please wait","最適化を行う為にキャッシュ情報を収集中です。\nしばらくお待ちください。");
                        }
                    });
                    int removeCount = downloadedList.size() - mAccount.getAttachmentCacheLimitCount();
                    mRemoveList = new ArrayList<String>();
                    int currentIndex = downloadedList.indexOf(mDispUid);

                    createRemoveList(downloadedList, currentIndex, removeCount);
                    dismissProgressDialog(mProgressDialog);
                    for (String uid : mRemoveList) {
                        SlideAttachment.clearCacheForAttachmentFile(mAccount, mFolder, uid);
                    }
                }
                createUidList(mDispUid, mAccount.getMessageLimitCountFromDb());
            }
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
        }
    }

    private void createRemoveList(ArrayList<String> downloadedList, int currentIndex, int removeCount) {
        if (0 == currentIndex) {
            //あるばあい リストの先頭で１つ後ろがないので逆にリストの後ろを消していく
            for (int i = (downloadedList.size() - 1); i >= (downloadedList.size() - removeCount); i--) {
                mRemoveList.add(downloadedList.get(i));
            }
        } else if (0 < currentIndex) {
            //あるばあい
            if (currentIndex >= removeCount) {
                //currentIndex-1 から -removeCount 件を削除リストにつっこむ
                for (int i = (currentIndex - 1); i >= (currentIndex - removeCount); i--) {
                    mRemoveList.add(downloadedList.get(i));
                }
            } else {
                System.out.println(Math.abs(currentIndex - removeCount));
                //abs(removeCount) の分だけ後ろからもってくる
                for (int i = (downloadedList.size() - 1); i >= (downloadedList.size() - Math.abs(currentIndex - removeCount)); i--) {
                    mRemoveList.add(downloadedList.get(i));
                }
                // currentIndex-1 から 先頭まで削除
                for (int i = (currentIndex - 1); i >= 0; i--) {
                    mRemoveList.add(downloadedList.get(i));
                }
            }
        } else {
            //ないばあい リストの一番後ろから件数分さくじょ
            for (int i = (downloadedList.size() - 1); i >= (downloadedList.size() - removeCount); i--) {
                mRemoveList.add(downloadedList.get(i));
            }
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
    private void dispSlide(final String uid) throws RakuRakuException {
        MessageBean messageBean = SlideMessage.getMessage(mAccount, mFolder, uid);
        if (!SlideCheck.isDownloadedAttachment(messageBean)) {
            mSlideShowHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSlideShow(uid);
                }
            });
            while (!isDownloaded) {
                //ダウソまち
            }
            isDownloaded = false;
            messageBean = SlideMessage.getMessage(mAccount, mFolder, uid);
        }
        dispSlide(messageBean);
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
        bundle.putString(MESSAGE_UID, messageBean.getUid());
        bundle.putString(MESSAGE_SUBJECT, messageBean.getSubject());
        bundle.putString(MESSAGE_SENDER_NAME, messageBean.getSenderName());
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
//        mInfo.setVisibility(View.VISIBLE);
        return imageView;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
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
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton("いいえ、待ち受けします",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onSlideShow(String uid) {
        new SlideShowTask(this).execute(uid);
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onClearAttachment(ArrayList<String> list) {
        new ClearAttachmentTask(this).execute(list.toString());
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class SlideShowTask extends AsyncTask<String, Integer, Void> implements DialogInterface.OnCancelListener {
        ProgressDialog dialog;
        Context context;

        public SlideShowTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please wait");
            dialog.setMessage("スライドショーデータをダウンロード中です。\nしばらくお待ちください。");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(this);
            dialog.setMax(100);
            dialog.setProgress(0);
            dialog.show();
        }

        /**
         * @return null
         */
        @Override
        protected Void doInBackground(String... params) {
            publishProgress(20);
            SlideAttachment.downloadAttachment(mAccount, mFolder, params[0]);
            publishProgress(40);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(Void tmp) {
            publishProgress(75);
            isDownloaded = true;
            publishProgress(100);
            onCancelled();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class ClearAttachmentTask extends AsyncTask<String, Integer, Void> implements DialogInterface.OnCancelListener {
        ProgressDialog dialog;
        Context context;

        public ClearAttachmentTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please wait");
            dialog.setMessage("キャッシュをクリアしています。\nしばらくお待ちください。");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(this);
            dialog.setMax(100);
            dialog.setProgress(0);
            dialog.show();
        }

        /**
         * @return null
         */
        @Override
        protected Void doInBackground(String... params) {
            publishProgress(10);
            for (String uid : params) {
                try {
                    SlideAttachment.clearCacheForAttachmentFile(mAccount, mFolder, uid);
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
                }
            }
            publishProgress(60);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(Void tmp) {
            publishProgress(75);
            isClear = true;
            publishProgress(100);
            onCancelled();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }
}
