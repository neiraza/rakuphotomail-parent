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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.setup.AccountSettings;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideCheck;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoConnectivityCheck;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideShow extends RakuPhotoActivity implements View.OnClickListener {

    /**
     * context
     */
    private Context mContext;
    /**
     *
     */
    /**
     * mAccount application account
     */
    private static Account mAccount;
    /**
     * folder name
     */
    private static String mFolder;
    /**
     * option menu
     */
    private boolean isOptionMenu = false;
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
     * Intent get/put folder name
     */
    private static final String EXTRA_START_INDEX = "startindex";
    /**
     * Intent get/put folder name
     */
    private static final String EXTRA_STOP_INDEX = "stopindex";
    /**
     * InstanceState startUid
     */
    private static final String INSTANCE_STATE_START_UID = "startUid";
    /**
     * InstanceState dispUid
     */
    private static final String INSTANCE_STATE_DISP_UID = "dispUid";
    /**
     *
     */
    private LinearLayout mInfo;
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
     * view subject
     */
    private TextView mSubject;
    /**
     * view subject
     */
    private TextView mSenderName;
    /**
     * Handler slide handler
     */
    private Handler mSlideHandler = new Handler();
    /**
     *
     */
    private String mDispUid = null;
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
     * Display Receive Date
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd h:mm a";
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
     *
     */
    private ArrayList<MessageBean> mSlideMessageBeanList = new ArrayList<MessageBean>();
    /**
     * Current Slide AttachmentBean list
     */
    private ArrayList<AttachmentBean> mAttachmentBeanList = new ArrayList<AttachmentBean>();
    /**
     * Current Slide MessageBean
     */
    private MessageBean mCurrentMessageBean = new MessageBean();
    /**
     * Handler
     */
    private Handler mSlideShowLoopHandler = new Handler();
    /**
     *
     */
    private Runnable mSlideShowLoopRunnable;
    /**
     *
     */
    private int mSlideMessageListIndex = 0;
    /**
     *
     */
    private int mAttachmentBeanListIndex = 0;
    /**
     * slide start UID
     */
    private String mSlideStartUid = null;
    /**
     * bitmap
     */
    private Bitmap mBitmap;
    /**
     *
     */
    private static int mPastMailCheckStartId = 0;
    /**
     *
     */
    private static int mPastMailCheckEndId = 0;
    /**
     *
     */
    private static int mServerSyncCount = 0;
    /**
     *
     */
    private MessageSyncTask mMessageSyncTask;
    /**
     *
     */
    private NewMailCheckTask mNewMailCheckTask;
    /**
     *
     */
    private DeleteMailCheckTask mDeleteMailCheckTask;
    /**
     *
     */
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm";
    /**
     *
     */
    private boolean isClick = true;

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
     * @param context    context
     * @param account    account info
     * @param folder     receive folder name
     * @param startIndex message index
     * @param stopIndex  message index
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void actionSlideShow(Context context, Account account, String folder, int startIndex, int stopIndex) {

        Intent intent = new Intent(context, GallerySlideShow.class);
        if (account != null) {
            intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
        }
        if (folder != null) {
            intent.putExtra(EXTRA_FOLDER, folder);
        }
        intent.putExtra(EXTRA_START_INDEX, startIndex);
        intent.putExtra(EXTRA_STOP_INDEX, stopIndex);
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
        onNewIntent(getIntent());
        setupViews();
        mServerSyncCount = mAccount.getMessageLimitCountFromRemote();

        if (mAccount.canSleep()) {
            setSleep();
        } else {
            cancelSleep();
        }

        int remoteMessageCount = setAppRunLatestInfo();
        // 初回時のみ新着メール最新情報を作成
        if (!RakuPhotoStringUtils.isNotBlank(mAccount.getNewMailCheckLatestUid())) {
            createNewMailCheckLatestInfo();
        }

        // 送信情報同期
        doSentFolderSync();

        if (mAccount.isAllSync()) {
            int tmpStart = mAccount.getCheckStartId();
            int tmpEnd = mAccount.getCheckEndId();
            // チェック処理中断から再開へ
            if (0 != tmpStart && 0 != tmpEnd) {
                mPastMailCheckStartId = tmpStart;
                mPastMailCheckEndId = tmpEnd;
            } else {
                createPastMailCheckLatestInfo();
                mPastMailCheckEndId = remoteMessageCount;
                mPastMailCheckStartId = mPastMailCheckEndId - mServerSyncCount;
                if (0 >= mPastMailCheckStartId) {
                    mPastMailCheckStartId = 1;
                }
            }
        } else {
            try {
                String localUid = mAccount.getPastMailCheckLatestUid();
                String remoteUid = mAccount.getAppRunLatestUid();
                if (RakuPhotoStringUtils.isNotBlank(localUid, remoteUid)) {
                    if (!localUid.equals(remoteUid)) {
                        mPastMailCheckEndId = remoteMessageCount;
                        mPastMailCheckStartId = mPastMailCheckEndId - mServerSyncCount;
                        if (0 >= mPastMailCheckStartId) {
                            mPastMailCheckStartId = setPastMailCheckStartId(localUid, remoteUid);
                            if (0 >= mPastMailCheckStartId) {
                                mPastMailCheckStartId = 1;
                            }
                        }
                        mAccount.setSync(true);
                    }
                } else {
                    Log.e(RakuPhotoMail.LOG_TAG, "全滅");
                    onStop();
                }
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            } catch (RakuRakuException rp) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + rp.getMessage());
            }
        }
    }

    private void setSleep() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void cancelSleep() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void doSentFolderSync() {
        if (RakuPhotoConnectivityCheck.isConnectivity(getApplicationContext())) {
            MessageSync.syncMailboxForCheckNewMail(mAccount, mAccount.getSentFolderName(), 0);
        } else {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#doSentFolderSync ネットワーク接続が切れています");
        }
    }

    private int setPastMailCheckStartId(String localUid, String remoteUid) throws RakuRakuException, MessagingException {
        String latestNewMailCheckUid = mAccount.getNewMailCheckLatestUid();
        int tmpNewMailCheckUid;
        if (RakuPhotoStringUtils.isNotBlank(latestNewMailCheckUid)) {
            tmpNewMailCheckUid = Integer.parseInt(latestNewMailCheckUid);
        } else {
            tmpNewMailCheckUid = 0;
        }
        int tmpLocalUid = Integer.parseInt(localUid);
        int tmpRemoteUid = Integer.parseInt(remoteUid);
        if (tmpRemoteUid > tmpNewMailCheckUid && tmpNewMailCheckUid > tmpLocalUid) {
            return MessageSync.getRemoteMessageId(mAccount, mFolder, latestNewMailCheckUid);
        } else {
            return MessageSync.getRemoteMessageId(mAccount, mFolder, localUid);
        }
    }

    private int setAppRunLatestInfo() {
        // App起動時の最新UID
        int tmpRemoteMessageCount = 0;
        String tmpRemoteUid = null;
        try {
            tmpRemoteMessageCount = MessageSync.getRemoteMessageCount(mAccount, mFolder);
            tmpRemoteUid = MessageSync.getRemoteUid(mAccount, mFolder, tmpRemoteMessageCount);
            mAccount.setAppRunLatestUid(tmpRemoteUid);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
        }
        // App起動時の最新日時
        mAccount.setAppRunLatestDate(new Date());
        return tmpRemoteMessageCount;
    }

    private void createNewMailCheckLatestInfo() {
        mAccount.setNewMailCheckLatestUid(mAccount.getAppRunLatestUid());
        mAccount.setNewMailCheckLatestDate(new Date());
    }

    private void updateNewMailCheckLatestInfo(String uid) {
        mAccount.setNewMailCheckLatestUid(uid);
        mAccount.setNewMailCheckLatestDate(new Date());
    }

    private void createPastMailCheckLatestInfo() {
        mAccount.setPastMailCheckLatestUid(mAccount.getAppRunLatestUid());
        mAccount.setPastMailCheckLatestDate(new Date());
    }

    /**
     * @param intent intent
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onNewIntent(Intent intent) {
        mAccount = Preferences.getPreferences(mContext).getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
        mFolder = intent.getStringExtra(EXTRA_FOLDER);
        mSlideStartUid = intent.getStringExtra(EXTRA_UID);
        mPastMailCheckStartId = intent.getIntExtra(EXTRA_START_INDEX, 0);
        isOptionMenu = false;
        setIntent(intent);
    }

    /**
     * view setup
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */

    private void setupViews() {
        mInfo = (LinearLayout) findViewById(R.id.gallery_info);
        setImageViewDefault();
        setImageViewEven();
        setImageViewOdd();
        mSubject = (TextView) findViewById(R.id.gallery_subject);
        mSenderName = (TextView) findViewById(R.id.gallery_sender_name);
        mDate = (TextView) findViewById(R.id.gallery_date);
        mAnswered = (TextView) findViewById(R.id.gallery_sent_flag);
        mAnsweredMark = (ImageView) findViewById(R.id.gallery_sent_flag_mark);
        mAnswered.setVisibility(View.GONE);
        mAnsweredMark.setVisibility(View.GONE);
    }

    /**
     * view setup
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setImageViewDefault() {
        mImageViewDefault = (ImageView) findViewById(R.id.gallery_attachment_picture_default);
        if ("".equals(mSlideStartUid)) {
            mImageViewDefault.setVisibility(View.VISIBLE);
            mInfo.setVisibility(View.GONE);
        } else {
            mImageViewDefault.setVisibility(View.GONE);
            mInfo.setVisibility(View.VISIBLE);
        }
    }

    /**
     * view setup
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setImageViewEven() {
        mImageViewEven = (ImageView) findViewById(R.id.gallery_attachment_picture_even);
        mImageViewEven.setOnClickListener(this);
        mImageViewEven.setVisibility(View.GONE);
    }

    /**
     * view setup
     */
    private void setImageViewOdd() {
        mImageViewOdd = (ImageView) findViewById(R.id.gallery_attachment_picture_odd);
        mImageViewOdd.setOnClickListener(this);
        mImageViewOdd.setVisibility(View.GONE);
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
        if (mAccount.canDispSlideShowInfo()) {
            mInfo.setVisibility(View.VISIBLE);
        } else {
            mInfo.setVisibility(View.GONE);
        }
        return imageView;
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isOptionMenu) {
            actionSlideShow(mContext, mAccount, mFolder, mDispUid);
            finish();
        } else {
            mMessageSyncTask = null;
            if (mAccount.isAllSync() || mAccount.isSync()) {
                startMessageSyncTask();
            } else {
                try {
                    mCurrentMessageBean = null;
                    int result = 0;
                    if (null != mSlideStartUid) {
                        result = getSlideMessageBeanList(mSlideStartUid, mAccount.getMessageLimitCountFromDb(), true);
                        mSlideStartUid = null;
                    }
                    if (0 == result) {
                        result = getSlideMessageBeanList(mDispUid, mAccount.getMessageLimitCountFromDb(), false);
                    }
                    if (0 == result) {
                        mDispUid = null;
                        result = getSlideMessageBeanList(null, mAccount.getMessageLimitCountFromDb(), false);
                    }
                    if (0 == result) {
                        doAlertForMessageIsNothing();
                    } else {
                        onRemove();
                        if (null != mSlideMessageBeanList && 0 != mSlideMessageBeanList.size()) {
                            mCurrentMessageBean = getSyncMessage(mSlideMessageBeanList.get(0));
                            slideShow();
                        } else {
                            actionSlideShow(mContext, mAccount, mFolder, null);
                        }
                    }
                } catch (RakuRakuException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                }
            }
        }
    }

    private void doAlertForMessageIsNothing() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setMessage(getString(R.string.warning_server_no_message_alert));
        alertDialogBuilder.setPositiveButton(getString(R.string.warning_server_no_message_alert_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.warning_server_no_message_alert_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAccount.setAllSync(true);
                        actionSlideShow(mContext, mAccount, mFolder, null);
                        finish();
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void slideShow() throws MessagingException {

        mSlideMessageListIndex = 0;
        mAttachmentBeanListIndex = 0;
        mSlideShowLoopRunnable = null;

        mSlideShowLoopRunnable = new Runnable() {
            @Override
            public void run() {
                // 予定分（mSlideMessageList）が終わったので再入荷希望
                if (null == mSlideMessageBeanList) {
                    mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
                    onResume();
                }
                if (mSlideMessageBeanList.size() < mSlideMessageListIndex + 1) {
                    mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
                    onResume();
                } else {
                    try {
                        // 予定分（mAttachmentBeanList）が終わったので、mSlideMessageListIndexで次メール希望
                        if (mAttachmentBeanList.size() < mAttachmentBeanListIndex + 1) {
                            mCurrentMessageBean = null;
                            mCurrentMessageBean = getSyncMessage(mSlideMessageBeanList.get(mSlideMessageListIndex));
                            mSlideMessageListIndex += 1;
                            mAttachmentBeanListIndex = 0;
                        }
                    } catch (MessagingException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                    } catch (RakuRakuException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
                    }

                    // がんばれ表示君
                    mAttachmentBeanList = null;
                    mAttachmentBeanList = mCurrentMessageBean.getAttachmentBeanList();
                    if (0 < mAttachmentBeanList.size()) {
                        AttachmentBean attachmentBean = mAttachmentBeanList.get(mAttachmentBeanListIndex);
                        mAttachmentBeanListIndex += 1;
                        if (SlideCheck.isSlide(attachmentBean)) {
                            try {
                                mBitmap = SlideAttachment.getBitmap(getApplicationContext(), getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
                                dispSlide(mCurrentMessageBean);
                            } catch (RakuRakuException e) {
                                Log.w(RakuPhotoMail.LOG_TAG, "UID:" + mCurrentMessageBean.getUid() + " " + getString(R.string.error_rakuraku_exception) + e.getMessage());
                            }
                            attachmentBean = null;
                        }
                    } else {
                        try {
                            dispSlide(mCurrentMessageBean);
                        } catch (RakuRakuException e) {
                            Log.w(RakuPhotoMail.LOG_TAG, "UID:" + mCurrentMessageBean.getUid() + " " + getString(R.string.error_rakuraku_exception) + e.getMessage());
                        }
                    }
                    //新着メールチェック起動判定
                    if (isNewMailCheck()) {
                        mAccount.setNewMailCheckLatestDate(new Date());
                        startDeleteMailCheckTask();
                    } else {
                        //次回起動
                        mSlideShowLoopHandler.postDelayed(this, mAccount.getSlideSleepTime());
                    }
                }
            }
        };
        if (null != mSlideMessageBeanList && 0 < mSlideMessageBeanList.size()) {
            //初回起動
            mSlideShowLoopHandler.postDelayed(mSlideShowLoopRunnable, 0);
        } else {
            onResume();
        }
    }

    private MessageBean getSyncMessage(MessageBean messageBean) throws MessagingException, RakuRakuException {
        MessageSync.syncMailForAttachmentDownload(mAccount, mFolder, messageBean);
        return SlideMessage.getMessage(mAccount, mFolder, messageBean.getUid());
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(final MessageBean messageBean) throws RakuRakuException {
        mDispUid = messageBean.getUid();
        mSubject.setText(messageBean.getSubject());
        String senderName = messageBean.getSenderName();

        if (null != senderName && !"".equals(senderName)) {
            mSenderName.setText(senderName.trim());
        } else {
            Log.w(RakuPhotoMail.LOG_TAG, "UID:" + messageBean.getUid() + " 送信者不明：" + senderName);
            mSenderName.setText("送信者不明");
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        mDate.setText(sdf.format(messageBean.getDate()));
        if (messageBean.isFlagAnswered()) {
            mAnswered.setVisibility(View.VISIBLE);
            mAnsweredMark.setVisibility(View.VISIBLE);
        } else {
            mAnswered.setVisibility(View.GONE);
            mAnsweredMark.setVisibility(View.GONE);
        }

        if (null != mBitmap) {
            setVisibilityImageView().setImageBitmap(mBitmap);
        } else {
            setVisibilityImageView().setImageBitmap(BitmapFactory.decodeResource(
                    getResources(), R.drawable.ucom_logo_black));
        }
    }

    private void onRemove() {
        ArrayList<String> tmpRemoveTargetList = SlideMessage.getMessageUidRemoveTarget(mAccount);
        ArrayList<String> removeTargetList = new ArrayList<String>();
        int cacheLimit = mAccount.getAttachmentCacheLimitCount();

        if (cacheLimit < tmpRemoveTargetList.size()) {
            for (int i = cacheLimit; i < tmpRemoveTargetList.size(); i++) {
                removeTargetList.add(tmpRemoveTargetList.get(i));
            }
        }
        tmpRemoveTargetList = null;

        if (0 < removeTargetList.size()) {
            removeCache(removeTargetList);
        }
        removeTargetList = null;
    }

    private void removeCache(ArrayList<String> removeTarget) {
        try {
            MessageSync.removeCache(mAccount, mFolder, removeTarget);
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        }
    }

    // 新着メールチェック起動判断用
    private boolean isNewMailCheck() {
        Date newMailCheckLatestDate = mAccount.getNewMailCheckLatestDate();
        if (null == newMailCheckLatestDate) {
            return false;
        }
        long syncTimeDuration = mAccount.getServerSyncTimeDuration();
        long latestSyncTime = newMailCheckLatestDate.getTime();
        long now = new Date().getTime();
        if (syncTimeDuration <= now - latestSyncTime) {
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_STATE_START_UID, mSlideStartUid);
        outState.putString(INSTANCE_STATE_DISP_UID, mDispUid);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceStage) {
        super.onRestoreInstanceState(savedInstanceStage);
        mSlideStartUid = savedInstanceStage.getString(INSTANCE_STATE_START_UID);
        mDispUid = savedInstanceStage.getString(INSTANCE_STATE_DISP_UID);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
        if (null != mMessageSyncTask) {
            mMessageSyncTask.cancel(false);
        }
        if (null != mNewMailCheckTask) {
            mNewMailCheckTask.cancel(false);
        }
        if (null != mDeleteMailCheckTask) {
            mDeleteMailCheckTask.cancel(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (isClick) {
            switch (v.getId()) {
                case R.id.gallery_attachment_picture_even:
                    try {
                        onSlideStop(mDispUid);
                    } catch (InterruptedException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
                    }
                    break;
                case R.id.gallery_attachment_picture_odd:
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
        mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
        GallerySlideStop.actionHandle(mContext, mAccount, mFolder, uid);
        finish();
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
        AccountSettings.actionSettings(mContext, mAccount);
    }

    private void startMessageSyncTask() {
        if (RakuPhotoConnectivityCheck.isConnectivity(getApplicationContext())) {
            mMessageSyncTask = new MessageSyncTask(mContext);
            if (AsyncTask.Status.RUNNING != mMessageSyncTask.getStatus()) {
                mMessageSyncTask.execute();
            }
        } else {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#startMessageSyncTask ネットワーク接続が切れています");
            Toast.makeText(getApplicationContext(), "ネットワーク接続が切れています", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class MessageSyncTask extends AsyncTask<Void, Void, Boolean> implements DialogInterface.OnCancelListener {
        private Context context;
        private ProgressDialog progressDialog;

        public MessageSyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            cancelSleep();
            if (!isFinishing()) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(getString(R.string.progress_please_wait));
                progressDialog.setMessage("メールサーバーをチェックしています(" + mPastMailCheckStartId + "~" + mPastMailCheckEndId + ")");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(true);
                progressDialog.setOnCancelListener(this);
                progressDialog.show();
            }
        }

        /**
         * @return null
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            if (null != progressDialog && !progressDialog.isIndeterminate() && !isFinishing()) {
                progressDialog.show();
            } else if (isFinishing()) {
                return false;
            }
            onRemove();
            try {
                MessageSync.syncMailbox(mAccount, mFolder, mPastMailCheckStartId, mPastMailCheckEndId);
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            }
            // Account に mPastMailCheckStartId & mPastMailCheckEndId !!
            mAccount.setCheckStartId(mPastMailCheckStartId);
            mAccount.setCheckEndId(mPastMailCheckEndId);

            if (mAccount.isAllSync()) {
                preparationAllSync();
            } else if (mAccount.isSync()) {
                try {
                    preparationSomeSync();
                } catch (RakuRakuException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                }
            }
            return result();
        }

        private void preparationAllSync() {
            if (1 != mPastMailCheckStartId) {
                mPastMailCheckEndId = mPastMailCheckStartId - 1;
                if (mPastMailCheckEndId <= 0) {
                    mPastMailCheckEndId = 1;
                }
                mPastMailCheckStartId = mPastMailCheckStartId - mServerSyncCount;
                if (mPastMailCheckStartId <= 0) {
                    mPastMailCheckStartId = 1;
                }
            } else {
                mAccount.setAllSync(false);
                clearCount();
            }
        }

        private void preparationSomeSync() throws RakuRakuException, MessagingException {
            String localUid = mAccount.getPastMailCheckLatestUid();
            String remoteUid = mAccount.getAppRunLatestUid();
            int messageId = setPastMailCheckStartId(localUid, remoteUid);

            if (0 >= messageId) {
                messageId = 1;
            }
            if (messageId != mPastMailCheckStartId) {
                mPastMailCheckEndId = mPastMailCheckStartId - 1;
                if (mPastMailCheckEndId < messageId) {
                    mPastMailCheckEndId = messageId;
                }
                mPastMailCheckStartId = mPastMailCheckStartId - mServerSyncCount;
                if (mPastMailCheckStartId < messageId) {
                    mPastMailCheckStartId = messageId;
                }
            } else {
                mAccount.setSync(false);
                clearCount();
            }
        }

        private boolean result() {
            int result = 0;
            if (null != mSlideStartUid) {
                result = getSlideMessageBeanList(mSlideStartUid, mAccount.getMessageLimitCountFromDb(), true);
                mSlideStartUid = null;
            }
            if (0 == result) {
                result = getSlideMessageBeanList(mDispUid, mAccount.getMessageLimitCountFromDb(), false);
            }
            if (0 == result) {
                mDispUid = null;
                result = getSlideMessageBeanList(null, mAccount.getMessageLimitCountFromDb(), false);
            }
            return 0 != result;
        }


        private void clearCount() {
            mPastMailCheckStartId = 0;
            mAccount.setCheckStartId(mPastMailCheckStartId);
            mPastMailCheckEndId = 0;
            mAccount.setCheckEndId(mPastMailCheckEndId);
        }

        @Override
        protected void onProgressUpdate(Void... params) {
        }

        @Override
        protected void onCancelled() {
            if (null != progressDialog && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            setSleepMode();
        }

        @Override
        protected void onPostExecute(Boolean isSlideMessage) {
            try {
                onCancelled();
                if (isSlideMessage) {
                    slideShow();
                } else {
                    onResume();
                }
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            }
            setSleepMode();
        }

        private void setSleepMode() {
            if (mAccount.canSleep()) {
                setSleep();
            } else {
                cancelSleep();
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    /**
     * DBから取得する際はstart~endの有限な固定数で取得する<br>
     * スライドショー的には最後に表示したUIDより古いメールを検索する
     *
     * @param startUid 開始基準位置（このUIDよりも小さいUIDを取得）
     * @param length   件数
     */
    private int getSlideMessageBeanList(String startUid, int length, boolean isIncludingUid) {
        mSlideMessageBeanList = null;
        try {
            mSlideMessageBeanList = SlideMessage.getMessageBeanList(mAccount, mFolder, startUid, length, isIncludingUid);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
        }
        return null == mSlideMessageBeanList ? 0 : mSlideMessageBeanList.size();
    }

    private void startNewMailCheckTask() {
        if (RakuPhotoConnectivityCheck.isConnectivity(getApplicationContext())) {
            mNewMailCheckTask = new NewMailCheckTask(mContext);
            if (AsyncTask.Status.RUNNING != mNewMailCheckTask.getStatus()) {
                mNewMailCheckTask.execute();
            }
        } else {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#startNewMailCheckTask ネットワーク接続が切れています");
            Toast.makeText(getApplicationContext(), "ネットワーク接続が切れています", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class NewMailCheckTask extends AsyncTask<Void, Void, Boolean> implements DialogInterface.OnCancelListener {
        private Context context;
        private ProgressDialog progressDialog;
        private boolean isAllSync = false;
        private boolean isSync = false;
        private String localOldUid;

        public NewMailCheckTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(getString(R.string.progress_please_wait));
            progressDialog.setMessage("新着メールについてサーバーをチェックしています");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(this);
            progressDialog.show();
        }

        /**
         * @return null
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            if (!progressDialog.isIndeterminate()) {
                progressDialog.show();
            }
            //チェック処理開始前に他の処理を止めたり
            mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
            if (mAccount.isAllSync()) {
                isAllSync = true;
                mAccount.setAllSync(false);
            }
            if (mAccount.isSync()) {
                isSync = true;
                mAccount.setSync(false);
            }
            isClick = false;
            try {
                localOldUid = SlideMessage.getHighestLocalUid(mAccount, mFolder);
                int remoteLatestId = MessageSync.getRemoteMessageId(mAccount, mFolder, MessageSync.getHighestRemoteUid(mAccount, mFolder));
                int localLatestId = MessageSync.getRemoteMessageId(mAccount, mFolder, mAccount.getAppRunLatestUid());
                if (localLatestId == remoteLatestId) {
                    return false;
                } else {
                    MessageSync.syncMailbox(mAccount, mFolder, localLatestId, remoteLatestId);
                }
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            } catch (RakuRakuException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + getString(R.string.error_rakuraku_exception_message) + e.getMessage());
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
        }

        @Override
        protected void onCancelled() {
            if (null != progressDialog && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(Boolean isNewMail) {
            try {
                String localLatestUid = SlideMessage.getHighestLocalUid(mAccount, mFolder);
                onCancelled();
                if (isNewMail && RakuPhotoStringUtils.isNotBlank(localLatestUid) && !localLatestUid.equals(localOldUid)) {
                    updateNewMailCheckLatestInfo(localLatestUid);
                    GallerySlideStop.actionHandle(mContext, mAccount, mFolder, localLatestUid);
                } else {
                    if (isAllSync) {
                        mAccount.setAllSync(true);
                    }
                    if (isSync) {
                        mAccount.setSync(true);
                    }
                    isClick = true;
                    actionSlideShow(mContext, mAccount, mFolder, mDispUid);
                }
                finish();
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    private void startDeleteMailCheckTask() {
        if (RakuPhotoConnectivityCheck.isConnectivity(getApplicationContext())) {
            mDeleteMailCheckTask = new DeleteMailCheckTask(mContext);
            if (AsyncTask.Status.RUNNING != mDeleteMailCheckTask.getStatus()) {
                mDeleteMailCheckTask.execute();
            }
        } else {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#startDeleteMailCheckTask ネットワーク接続が切れています");
            Toast.makeText(getApplicationContext(), "ネットワーク接続が切れています", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class DeleteMailCheckTask extends AsyncTask<Void, Void, ArrayList<String>> implements DialogInterface.OnCancelListener {
        Context context;
        ProgressDialog progressDialog;

        public DeleteMailCheckTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(getString(R.string.progress_please_wait));
            progressDialog.setMessage("削除済みメールについてサーバーをチェックしています");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(this);
            progressDialog.show();
        }

        /**
         * @return null
         */
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            if (!progressDialog.isIndeterminate()) {
                progressDialog.show();
            }
            ArrayList<String> remoteUidList;
            ArrayList<String> localUidList;
            ArrayList<String> deleteList = new ArrayList<String>();

            try {
                int localLatestId = MessageSync.getRemoteMessageId(mAccount, mFolder, mAccount.getAppRunLatestUid());
                remoteUidList = MessageSync.getRemoteUidList(mAccount, mFolder, 1, localLatestId);
                localUidList = SlideMessage.getUidList(mAccount, mFolder);

                for (String localUid : localUidList) {
                    if (!remoteUidList.contains(localUid)) {
                        deleteList.add(localUid);
                    }
                }
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            } catch (RakuRakuException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + getString(R.string.error_rakuraku_exception_message) + e.getMessage());
            } finally {
                remoteUidList = null;
                localUidList = null;
            }
            return deleteList;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
        }

        @Override
        protected void onCancelled() {
            if (null != progressDialog && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> deleteList) {
            onCancelled();
            if (0 < deleteList.size()) {
                try {
                    SlideMessage.deleteMessages(mAccount, mFolder, deleteList);
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                }
            }
            startNewMailCheckTask();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }

}
