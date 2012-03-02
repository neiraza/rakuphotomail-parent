/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.util;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.GallerySlideStop;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;
import jp.co.fttx.rakuphotomail.activity.setup.AccountSettings;
import jp.co.fttx.rakuphotomail.mail.Flag;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.Attachments;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.MessageInfo;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideCheck;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideShowOld extends RakuPhotoActivity implements View.OnClickListener {

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
    private static Account mAccount;
    /**
     * folder name
     */
    private static String mFolder;
    /**
     * start UID
     */
    private String mStartUid;
    /**
     * context
     */
    private Context mContext;
    /**
     *
     */
    private LinearLayout mInfo;
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
    private ArrayList<String> mSlideAllUidList = new ArrayList<String>();
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
     * bitmap
     */
    private Bitmap mBitmap;
    /**
     * Thread SlideShow
     */
    private SlideShowThread mSlideShowThread;
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
    private static boolean isDownloaded = false;
    /**
     *
     */
    private boolean isOptionMenu = false;
    /**
     *
     */
    private static boolean isClear = false;
    /**
     *
     */
    private ArrayList<String> mRemoveList = new ArrayList<String>();
    /**
     *
     */
    private static long serverSyncTimeDuration;
    /**
     *
     */
    private static long serverSyncInitStartTimeDuration;


    /**
     * @param context context
     * @param account account info
     * @param folder  receive folder name
     * @param uid     message uid
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void actionSlideShow(Context context, Account account, String folder, String uid) {
        Log.d("pgr", "actionSlideShow start");

        Intent intent = new Intent(context, GallerySlideShowOld.class);
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
        Log.d("pgr", "onCreate start");

        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show);
        mProgressDialog = new ProgressDialog(mContext);
        setUpProgressDialog(mProgressDialog, "Please wait", "スライドショー情報をサーバーと同期中です。\n完了次第、スライドショーを開始します。\nしばらくお待ちください。");
        onNewIntent(getIntent());
        setupViews();
        Log.d("pgr", "onCreate doAllFolderSync start");
        doAllFolderSync();
        Log.d("pgr", "onCreate doAllFolderSync end");
//        mAllUidList = getUidList(null, 0);
        Log.d("pgr", "onCreate mAllUidList created");
        setupSlideShowThread();

        serverSyncTimeDuration = mAccount.getServerSyncTimeDuration();
        serverSyncInitStartTimeDuration = mAccount.getServerSyncInitStartTimeDuration();

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("pgr", "mTimer start");

                startProgressDialogHandler("Please wait", "サーバーと同期し、新着メールをチェックしています。\nしばらくお待ちください。");
                mIsRepeatUidList = false;
                if (mSlideShowThread.isAlive()) {
                    mSlideShowThread.interrupt();
                }
                Log.d("pgr", "mTimer mSlideShowThread 割り込み");

                // 同期処理で新着メールを見つけられた場合
                String newMailUid = MessageSync.syncMailboxForCheckNewMail(mAccount, mFolder, mAccount.getMessageLimitCountFromRemote());

                doSentFolderSync();

                Log.d("pgr", "mTimer sync 完了");


                //TODO これ必要か？
//                mSlideAllUidList = createUidList();

                boolean isSlideShow = false;

                if (null != newMailUid && !"".equals(newMailUid) && isSlide(newMailUid)) {
                    isSlideShow = true;
//                    mIsRepeatUidList = false;
//                    if (mSlideShowThread.isAlive()) {
//                        mSlideShowThread.interrupt();
//                    } else {
//                    mDispUid = newMailUid;
//                    }
                    try {
                        onSlideStop(newMailUid);
                    } catch (InterruptedException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
                    }
//                    mAllUidList.clear();
//                    mAllUidList = getUidList(null, 0);
                    finish();
                    Log.d("pgr", "mTimer after finish");
                }

//                if (!isSlideShow) {
//                    // サーバーとつながってる状態で新着メールがローカル取り込み完了している場合
//                    ArrayList<String> newUidList = getUidList(null, mAccount.getMessageLimitCountFromDb());
//                    for (String uid : newUidList) {
//                        if (!mAllUidList.contains(uid) && isSlide(uid)) {
//                            isSlideShow = true;
////                            mIsRepeatUidList = false;
////                            if (mSlideShowThread.isAlive()) {
////                                mSlideShowThread.interrupt();
////                            } else {
//                            mDispUid = uid;
////                            }
//
//                            try {
//                                onSlideStop(newMailUid);
//                            } catch (InterruptedException e) {
//                                Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
//                            }
////                            mAllUidList.clear();
////                            mAllUidList = getUidList(null, 0);
//                            finish();
//                        }
//                    }
//                }
                if (!isSlideShow) {
                    Log.d("pgr", "mTimer GallerySlideShow#actionSlideShow");
                    GallerySlideShowOld.actionSlideShow(mContext, mAccount, mFolder, mDispUid);
                    dismissProgressDialog(mProgressDialog);
                }
            }
        }, serverSyncInitStartTimeDuration, serverSyncTimeDuration);
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

    private void setUpProgressDialog(ProgressDialog progressDialog, String title, String message) {
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
        mInfo = (LinearLayout) findViewById(R.id.gallery_info);
        setImageViewDefault();
        setImageViewEven();
        setImageViewOdd();
        mSubject = (TextView) findViewById(R.id.gallery_subject);
        mSenderName = (TextView) findViewById(R.id.gallery_sender_name);
        mSlideHandler = new Handler() {
            public void handleMessage(Message msg) {
                setVisibilityImageView().setImageBitmap((Bitmap) msg.obj);
                Bundle bundle = msg.getData();
                mDispUid = bundle.get(MESSAGE_UID).toString();
                mSubject.setText(bundle.get(MESSAGE_SUBJECT).toString());
                String senderName = bundle.get(MESSAGE_SENDER_NAME).toString();
                mSenderName.setText(senderName.trim());
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
        mAnsweredMark = (ImageView) findViewById(R.id.gallery_sent_flag_mark);
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
        isOptionMenu = false;
        setIntent(intent);
    }

    private ArrayList<String> createUidList() {

        List<MessageInfo> messageInfoList = SlideMessage.getMessageInfoList(mAccount, mFolder, null, 0);
        ArrayList<String> uidList = new ArrayList<String>();
        if (null != messageInfoList || 0 != messageInfoList.size()) {
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
        } else {
            Log.w(RakuPhotoMail.LOG_TAG, "現在、サーバー上に受信メールが存在しません");
            dismissProgressDialog(mProgressDialog);
            onAlertNoMessage();
            return uidList;
        }
        if (null == uidList || 0 == uidList.size()) {
            Log.w(RakuPhotoMail.LOG_TAG, "現在、サーバー上にスライドショー可能なメールが存在しません");
            dismissProgressDialog(mProgressDialog);
            onAlertNoMessage();
            return uidList;
        }
        return uidList;
    }

    private ArrayList<String> setStartUidList(ArrayList<String> dest, String currentUid, long limitCount) {
        ArrayList<String> src = new ArrayList<String>();

        if (null != currentUid) {
            int loopStartTerms = dest.indexOf(currentUid);
            int loopEndTerms = (int) limitCount + loopStartTerms;
            addList(dest, src, loopStartTerms, loopEndTerms);

            int srcMaxSize = src.size();
            //残数分を頭から獲得しとく
            if (srcMaxSize < limitCount) {
                int loopEndTerms2 = (int) limitCount - srcMaxSize;
                addList(dest, src, 0, loopEndTerms2);
            }
        } else {
            addList(dest, src, 0, (int) limitCount);
        }
        return src;
    }

    private void addList(ArrayList<String> dest, ArrayList<String> src, int loopStartTerms, int loopEndTerms) {
        for (int i = loopStartTerms; i < loopEndTerms; i++) {
            if (dest.size() - 1 >= i && !src.contains(dest.get(i))) {
                src.add(dest.get(i));
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

    private class SlideShowThread extends Thread {
        public SlideShowThread(Runnable runnable) {
            super(runnable);
        }

        public void sleepKing(long time) throws InterruptedException {
            super.sleep(time);
        }

    }

    private void setupSlideShowThread() {
        mSlideShowThread = new SlideShowThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("pgr", "setupSlideShowThread loopInfinite start");
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
        Log.d("pgr", "onResume start");

        super.onResume();
        if (isOptionMenu) {
            actionSlideShow(mContext, mAccount, mFolder, mDispUid);
            finish();
        } else {
            mSlideAllUidList = createUidList();
            if (null != mSlideAllUidList && 0 < mSlideAllUidList.size()) {
                onSlide();
            }
        }

        //TODO log
        if (0 != mAllUidList.size()) {
            Log.d("pgr", "onResume mAllUidList:" + Arrays.toString(mAllUidList.toArray()));
        }
        if (0 != mSlideAllUidList.size()) {
            Log.d("pgr", "onResume mSlideAllUidList:" + Arrays.toString(mSlideAllUidList.toArray()));
        }
    }

    /**
     * slide.
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void onSlide() {
        if (!mSlideShowThread.isAlive()) {
            mSlideShowThread.start();
        }
    }

    /**
     * loooooooop.
     *
     * @throws jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException  rakuraku!
     * @throws jp.co.fttx.rakuphotomail.mail.MessagingException me
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void loopInfinite() throws RakuRakuException, MessagingException {
        try {
            while (mIsRepeatUidList) {
                ArrayList<String> uidList = new ArrayList<String>();
                if (!"".equals(mStartUid)) {
                    uidList = setStartUidList(mSlideAllUidList, mStartUid, mAccount.getMessageLimitCountFromDb());
                    mStartUid = "";
                } else if (!"".equals(mDispUid)) {
                    uidList = setStartUidList(mSlideAllUidList, mDispUid, mAccount.getMessageLimitCountFromDb());
                } else {
                    uidList = setStartUidList(mSlideAllUidList, null, mAccount.getMessageLimitCountFromDb());
                }
                dismissProgressDialog(mProgressDialog);

                LocalStore localStore = mAccount.getLocalStore();
                LocalStore.LocalFolder localFolder = localStore.getFolder(mFolder);
                for (int i = 0; i < uidList.size(); i++) {
                    String uid = uidList.get(i);
                    jp.co.fttx.rakuphotomail.mail.Message message = localFolder.getMessage(uid);
                    if (!message.isSet(Flag.X_DOWNLOADED_FULL)) {
                        startProgressDialogHandler("Please wait", "スライドショー情報をサーバーと同期中です。\nしばらくお待ちください。");
                        SlideAttachment.downloadAttachment(mAccount, mFolder, uid);
                        dismissProgressDialog(mProgressDialog);
                    }
                }

                //slide show
                loop(uidList);

                ArrayList<String> downloadedList = SlideMessage.getMessageUidRemoveTarget(mAccount);
                if (downloadedList.size() > mAccount.getAttachmentCacheLimitCount()) {
                    startProgressDialogHandler("Please wait", "最適化を行う為にキャッシュ情報を収集中です。\nしばらくお待ちください。");
                    int removeCount = downloadedList.size() - mAccount.getAttachmentCacheLimitCount();
                    mRemoveList = new ArrayList<String>();
                    int currentIndex = downloadedList.indexOf(mDispUid);
                    createRemoveList(downloadedList, currentIndex, removeCount);
                    for (String uid : mRemoveList) {
                        SlideAttachment.clearCacheForAttachmentFile(mAccount, mFolder, uid);
                    }
                    dismissProgressDialog(mProgressDialog);
                }
            }
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
        }
    }

    private void startProgressDialogHandler(final String title, final String message) {
        mProgressHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = new ProgressDialog(mContext);
                setUpProgressDialog(mProgressDialog, title, message);
            }
        });
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
     * @throws jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void loop(ArrayList<String> uidList) throws RakuRakuException {
        for (String uid : uidList) {
            dispSlide(uid);
            if (!mIsRepeatUidList) {
                return;
            }
        }
    }

    /**
     * @param uid mail uid
     * @throws jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException exception
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
                try {
                    mSlideShowThread.sleepKing(1500L);
                } catch (InterruptedException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
                }
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
        dismissProgressDialog(mProgressDialog);
        ArrayList<AttachmentBean> attachmentBeanList = messageBean.getAttachmentBeanList();
        try {
            for (AttachmentBean attachmentBean : attachmentBeanList) {
                if (SlideCheck.isSlide(attachmentBean)) {
                    mBitmap = SlideAttachment.getBitmap(getApplicationContext(), getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
                    if (null == mBitmap) {
                        return;
                    }
                    Message msg = setSendMessage(messageBean);
                    msg.obj = mBitmap;
                    mSlideHandler.sendMessage(msg);
                    sleepSlide(mAccount.getSlideSleepTime());
                }
            }
        } catch (RakuRakuException rre) {
            //TODO メモリエラー対策）いったん頭に戻ってみよう、すっきりするかもよ
            actionSlideShow(mContext, mAccount, mFolder, mDispUid);
        }
    }

    /**
     * @param messageBean MessageBean
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
            mSlideShowThread.sleepKing(sleepTime);
        } catch (InterruptedException e) {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#sleepSlide 割り込み開始の為、許容範囲内です");
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
            mIsRepeatUidList = false;
            mSlideShowThread.interrupt();
        } catch (ClassCastException cce) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onStop Error:" + cce.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (null != mTimer) {
                mTimer.cancel();
                mTimer = null;
            }
        } catch (ClassCastException cce) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onDestroy Error:" + cce.getMessage());
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
                    Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
                }
                break;
            case ID_GALLERY_ATTACHMENT_PICTURE_ODD:
                try {
                    onSlideStop(mDispUid);
                } catch (InterruptedException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
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
        Log.d("pgr", "onSlideStop start");

        startProgressDialogHandler("Please wait", "スライドショーを停止中です。\nしばらくお待ちください。");
        mIsRepeatUidList = false;
        if (mSlideShowThread.isAlive()) {
            mSlideShowThread.interrupt();
        }
        mTimer.cancel();
        mTimer = null;
        GallerySlideStop.actionHandle(mContext, mAccount, mFolder, uid);
        dismissProgressDialog(mProgressDialog);
        finish();
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
    private static class SlideShowTask extends AsyncTask<String, Integer, Void> implements DialogInterface.OnCancelListener {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.message_list_option, menu);
        menu.findItem(R.id.buttons_disabled).setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        isOptionMenu = true;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.account_settings: {
                onEditAccount();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void onEditAccount() {
        AccountSettings.actionSettings(this, mAccount);
    }
}