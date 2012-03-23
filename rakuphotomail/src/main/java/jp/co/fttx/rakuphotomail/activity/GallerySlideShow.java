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
import jp.co.fttx.rakuphotomail.activity.setup.AccountSettings;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideCheck;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
//    /**
//     * start UID
//     */
//    private String mStartUid;
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

    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm";
    private boolean isClick = true;
//    private boolean isTmpAllSync = false;
//    private boolean isTmpSync = false;

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
        Log.d("ahokato", "GallerySlideShow#actionSlideShow(index) start");

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
        Log.d("ahokato", "GallerySlideShow#onCreate start");
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show);
        onNewIntent(getIntent());
        setupViews();
        mServerSyncCount = mAccount.getMessageLimitCountFromRemote();

        //TODO 2012/03/21 add
        int remoteMessageCount = setAppRunLatestInfo();
        // 初回時のみ新着メール最新情報を作成
        if (!RakuPhotoStringUtils.isNotBlank(mAccount.getNewMailCheckLatestUid())) {
            createNewMailCheckLatestInfo();
        }

        if (mAccount.isAllSync()) {
            int tmpStart = mAccount.getCheckStartId();
            int tmpEnd = mAccount.getCheckEndId();
            // チェック処理中断から再開へ
            if (0 != tmpStart && 0 != tmpEnd) {
                Log.d("ahokato", "GallerySlideShow#onCreate 処理中断から再開します");
                mPastMailCheckStartId = tmpStart;
                mPastMailCheckEndId = tmpEnd;
            } else {
                Log.d("ahokato", "GallerySlideShow#onCreate アプリ起動時の初回処理です");
                //TODO 2012/03/21 modify
                createPastMailCheckLatestInfo();
                mPastMailCheckEndId = remoteMessageCount;
                mPastMailCheckStartId = mPastMailCheckEndId - mServerSyncCount;
                if (0 >= mPastMailCheckStartId) {
                    mPastMailCheckStartId = 1;
                }
            }
            Log.d("ahokato", "GallerySlideShow#onCreate mPastMailCheckStartId:" + mPastMailCheckStartId);
            Log.d("ahokato", "GallerySlideShow#onCreate mPastMailCheckEndId:" + mPastMailCheckEndId);
        } else {
            try {
                String localUid = mAccount.getPastMailCheckLatestUid();
                String remoteUid = mAccount.getAppRunLatestUid();
                Log.d("ahokato", "GallerySlideShow#onCreate localUid:" + localUid);
                Log.d("ahokato", "GallerySlideShow#onCreate remoteUid:" + remoteUid);

                if (RakuPhotoStringUtils.isNotBlank(localUid, remoteUid)) {
                    if (localUid.equals(remoteUid)) {
                        Log.d("ahokato", "GallerySlideShow#onCreate ローカル情報が最新のため、同期は行いません");
                        int result = getSlideMessageBeanList(null, mServerSyncCount);
                        if (0 == result) {
                            doAlertForMessageIsNothing();
                        }
                    } else {
                        Log.d("ahokato", "GallerySlideShow#onCreate ローカル情報が最新ではない恐れが有るため、同期を行います");
                        //TODO 2012/03/21 modify
                        mPastMailCheckEndId = remoteMessageCount;
                        mPastMailCheckStartId = mPastMailCheckEndId - mServerSyncCount;
                        if (0 >= mPastMailCheckStartId) {
                            mPastMailCheckStartId = MessageSync.getRemoteMessageId(mAccount, mFolder, mAccount.getPastMailCheckLatestUid());
                            if (0 >= mPastMailCheckStartId) {
                                mPastMailCheckStartId = 1;
                            }
                        }
                        mAccount.setSync(true);
                        Log.d("ahokato", "GallerySlideShow#onCreate mPastMailCheckStartId:" + mPastMailCheckStartId);
                        Log.d("ahokato", "GallerySlideShow#onCreate mPastMailCheckEndId:" + mPastMailCheckEndId);
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
        Log.d("ahokato", "GallerySlideShow#createNewMailCheckLatestInfo start");
        // 新着メールチェックの最新UID
        mAccount.setNewMailCheckLatestUid(mAccount.getAppRunLatestUid());
        // 新着メールチェックの最新日時
        mAccount.setNewMailCheckLatestDate(new Date());
    }

    private void updateNewMailCheckLatestInfo(String uid) {
        Log.d("ahokato", "GallerySlideShow#updateNewMailCheckLatestInfo start");
        // 新着メールチェックの最新UID
        mAccount.setNewMailCheckLatestUid(uid);
        // 新着メールチェックの最新日時
        mAccount.setNewMailCheckLatestDate(new Date());
    }

    private void createPastMailCheckLatestInfo() {
        Log.d("ahokato", "GallerySlideShow#createPastMailCheckLatestInfo start");
        // 新着メールチェックの最新UID
        mAccount.setPastMailCheckLatestUid(mAccount.getAppRunLatestUid());
        // 新着メールチェックの最新日時
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
        mInfo.setVisibility(View.VISIBLE);
        return imageView;
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onResume() {
        Log.d("ahokato", "GallerySlideShow#onResume start isOptionMenu:" + isOptionMenu);
        super.onResume();
        if (isOptionMenu) {
            actionSlideShow(mContext, mAccount, mFolder, mDispUid);
            finish();
        } else {
            mMessageSyncTask = null;
            Log.d("ahokato", "GallerySlideShow#onResume mAccount.isAllSync():" + mAccount.isAllSync());
            Log.d("ahokato", "GallerySlideShow#onResume mAccount.isSync():" + mAccount.isSync());
            if (mAccount.isAllSync() || mAccount.isSync()) {
                startMessageSyncTask();
            } else {
                try {
                    mCurrentMessageBean = null;
                    int result1 = getSlideMessageBeanList(mDispUid, mAccount.getMessageLimitCountFromDb());
                    if (0 == result1) {
                        mDispUid = null;
                        int result2 = getSlideMessageBeanList(null, mAccount.getMessageLimitCountFromDb());
                        Log.d("ahokato", "MessageSyncTask#doInBackground result2:" + result2);
                        if (0 == result2) {
                            doAlertForMessageIsNothing();
                        }
                    }
                    onRemove();
                    //TODO Sync !!!
                    mCurrentMessageBean = getSyncMessage(mSlideMessageBeanList.get(0));
                    slideShow();
                } catch (RakuRakuException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                }
            }
        }
    }

    private void doAlertForMessageIsNothing() {
        Log.d("ahokato", "GallerySlideShow#doAlertForMessageIsNothing start");
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
                        onResume();
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Log.d("ahokato", "GallerySlideShow#doAlertForMessageIsNothing end");
    }

    private void slideShow() throws MessagingException {
        Log.d("ahokato", "GallerySlideShow#slideShow start");

        mSlideMessageListIndex = 0;
        mAttachmentBeanListIndex = 0;
        mSlideShowLoopRunnable = null;

        mSlideShowLoopRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("ahokato", "GallerySlideShow#slideShow mSlideMessageListIndex:" + mSlideMessageListIndex);
                Log.d("ahokato", "GallerySlideShow#slideShow mSlideMessageBeanList.size():" + (mSlideMessageBeanList.size()));
                // 予定分（mSlideMessageList）が終わったので再入荷希望
                if (mSlideMessageBeanList.size() < mSlideMessageListIndex + 1) {
                    Log.d("ahokato", "GallerySlideShow#slideShow 予定分（mSlideMessageList）が終わったので再入荷希望");
                    mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
                    onResume();
                } else {
                    Log.d("ahokato", "GallerySlideShow#slideShow mAttachmentBeanListIndex:" + mAttachmentBeanListIndex);
                    Log.d("ahokato", "GallerySlideShow#slideShow mAttachmentBeanList.size():" + (mAttachmentBeanList.size()));
                    try {
                        // 予定分（mAttachmentBeanList）が終わったので、mSlideMessageListIndexで次メール希望
                        if (mAttachmentBeanList.size() < mAttachmentBeanListIndex + 1) {
                            Log.d("ahokato", "GallerySlideShow#slideShow 予定分（mAttachmentBeanList）が終わったので、mSlideMessageListIndexで次メール希望");
                            mCurrentMessageBean = null;
                            mCurrentMessageBean = getSyncMessage(mSlideMessageBeanList.get(mSlideMessageListIndex));
                            mSlideMessageListIndex += 1;
                            mAttachmentBeanListIndex = 0;
                        } else {
                            // 再ダウンロード確認＆希望
                            Log.d("ahokato", "GallerySlideShow#slideShow 再ダウンロード確認&希望");
                        }
                    } catch (MessagingException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                    } catch (RakuRakuException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
                    }

                    Log.d("ahokato", "GallerySlideShow#slideShow mCurrentMessageBean:" + mCurrentMessageBean.getUid());
                    // がんばれ表示君
                    mAttachmentBeanList = null;
                    mAttachmentBeanList = mCurrentMessageBean.getAttachmentBeanList();
                    if (0 < mAttachmentBeanList.size()) {
                        AttachmentBean attachmentBean = mAttachmentBeanList.get(mAttachmentBeanListIndex);
                        mAttachmentBeanListIndex += 1;
                        if (SlideCheck.isSlide(attachmentBean)) {
                            try {
                                Log.d("ahokato", "GallerySlideShow#slideShow mSlideShowLoopRunnable attachmentBean.getId():" + attachmentBean.getId());
                                Log.d("ahokato", "GallerySlideShow#slideShow mSlideShowLoopRunnable attachmentBean.getContentUrl():" + attachmentBean.getContentUrl());
                                mBitmap = SlideAttachment.getBitmap(getApplicationContext(), getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
                                dispSlide(mCurrentMessageBean);
                                Log.d("ahokato", "GallerySlideShow#slideShow mSlideShowLoopRunnable image:" + attachmentBean.getName());
                            } catch (FileNotFoundException e) {
                                Log.w(RakuPhotoMail.LOG_TAG, "UID:" + mCurrentMessageBean.getUid() + " " + e.getMessage());
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
                        Log.d("ahokato", "GallerySlideShow#slideShow 新着メールを確認するお時間です");
                        mAccount.setNewMailCheckLatestDate(new Date());
                        startNewMailCheckTask();
                    } else {
                        Log.d("ahokato", "GallerySlideShow#slideShow スライドショーのスケジューリングを行います");
                        //次回起動
                        mSlideShowLoopHandler.postDelayed(this, mAccount.getSlideSleepTime());
                    }
                }
            }
        };
        if (0 < mSlideMessageBeanList.size()) {
            //初回起動
            Log.d("ahokato", "GallerySlideShow#slideShow 初回起動");
            mSlideShowLoopHandler.postDelayed(mSlideShowLoopRunnable, 0);
        } else {
            Log.d("ahokato", "GallerySlideShow#slideShow 初回起動しませんでした");
            onResume();
        }
    }

    private MessageBean getSyncMessage(MessageBean messageBean) throws MessagingException, RakuRakuException {
        Log.d("ahokato", "GallerySlideShow#dispSlide(MessageBean messageBean)");
        MessageSync.syncMailForAttachmentDownload(mAccount, mFolder, messageBean);
        return SlideMessage.getMessage(mAccount, mFolder, messageBean.getUid());
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(final MessageBean messageBean) throws RakuRakuException {
        Log.d("ahokato", "GallerySlideShow#dispSlide start");
        mDispUid = messageBean.getUid();
        mSubject.setText(messageBean.getSubject());
        String senderName = messageBean.getSenderName();

        if (null != senderName && !"".equals(senderName)) {
            Log.d("ahokato", "GallerySlideShow#dispSlide :" + senderName.trim());
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
        Log.d("ahokato", "GallerySlideShow#onRemove start");
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
        Log.d("ahokato", "GallerySlideShow#removeCache start");
        try {
            Log.d("ahokato", "GallerySlideShow#removeCache start" + Arrays.toString(removeTarget.toArray(new String[removeTarget.size()])));
            MessageSync.removeCache(mAccount, mFolder, removeTarget);
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        }
    }

    // 新着メールチェック起動判断用
    private boolean isNewMailCheck() {
        Log.d("ahokato", "GallerySlideShow#isNewMailCheck start");

        Date newMailCheckLatestDate = mAccount.getNewMailCheckLatestDate();
        Log.d("ahokato", "GallerySlideShow#isNewMailCheck newMailCheckLatestDate:" + newMailCheckLatestDate);

        if (null == newMailCheckLatestDate) {
            Log.d("ahokato", "GallerySlideShow#isNewMailCheck False");
            return false;
        }
        long syncTimeDuration = mAccount.getServerSyncTimeDuration();
        long latestSyncTime = newMailCheckLatestDate.getTime();
        long now = new Date().getTime();
        Log.d("ahokato", "GallerySlideShow#isNewMailCheck syncTimeDuration:" + syncTimeDuration);
        Log.d("ahokato", "GallerySlideShow#isNewMailCheck latestSyncTime:" + latestSyncTime);
        Log.d("ahokato", "GallerySlideShow#isNewMailCheck now:" + now);

        if (syncTimeDuration <= now - latestSyncTime) {
            Log.d("ahokato", "GallerySlideShow#isNewMailCheck True!");
            return true;
        }

        Log.d("ahokato", "GallerySlideShow#isNewMailCheck False");
        return false;
    }

    @Override
    public void onStop() {
        Log.d("ahokato", "GallerySlideShow#onStop start");
        super.onStop();
        mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
        if (null != mMessageSyncTask) {
            mMessageSyncTask.cancel(false);
        }
        if (null != mNewMailCheckTask) {
            mNewMailCheckTask.cancel(false);
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
        Log.d("ahokato", "GallerySlideShow#startMessageSyncTask start");
        mMessageSyncTask = new MessageSyncTask(mContext);
        if (AsyncTask.Status.RUNNING != mMessageSyncTask.getStatus()) {
            mMessageSyncTask.execute();
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class MessageSyncTask extends AsyncTask<Void, Void, Boolean> implements DialogInterface.OnCancelListener {
        Context context;
        ProgressDialog progressDialog;

        public MessageSyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(getString(R.string.progress_please_wait));
            progressDialog.setMessage("メールサーバーをチェックしています(" + mPastMailCheckStartId + "~" + mPastMailCheckEndId + ")");
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
            Log.d("ahokato", "MessageSyncTask#doInBackground mPastMailCheckStartId:" + mPastMailCheckStartId);
            Log.d("ahokato", "MessageSyncTask#doInBackground mPastMailCheckEndId:" + mPastMailCheckEndId);

            Log.d("ahokato", "MessageSyncTask#doInBackground removeしまーす");
            onRemove();
            Log.d("ahokato", "MessageSyncTask#doInBackground syncしまーす");
            //TODO Sync!!!
            MessageSync.syncMailbox(mAccount, mFolder, mPastMailCheckStartId, mPastMailCheckEndId);
            // Account に mPastMailCheckStartId & mPastMailCheckEndId !!
            mAccount.setCheckStartId(mPastMailCheckStartId);
            mAccount.setCheckEndId(mPastMailCheckEndId);
            Log.d("ahokato", "MessageSyncTask#doInBackground syncしましたー");

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

            Log.d("ahokato", "MessageSyncTask#doInBackground mPastMailCheckStartId:" + mPastMailCheckStartId);
            Log.d("ahokato", "MessageSyncTask#doInBackground mPastMailCheckEndId:" + mPastMailCheckEndId);

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
            int messageId = MessageSync.getRemoteMessageId(mAccount, mFolder, mAccount.getPastMailCheckLatestUid());
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
            int result1 = getSlideMessageBeanList(mDispUid, mAccount.getMessageLimitCountFromDb());
            Log.d("ahokato", "MessageSyncTask#result result1:" + result1);
            if (0 == result1) {
                mDispUid = null;
                int result2 = getSlideMessageBeanList(null, mAccount.getMessageLimitCountFromDb());
                Log.d("ahokato", "MessageSyncTask#result result2:" + result2);
                if (0 == result2) {
                    return false;
                }
            }
            return true;
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
        }

        @Override
        protected void onPostExecute(Boolean isSlideMessage) {
            try {
                Log.d("ahokato", "MessageSyncTask#onPostExecute isSlideMessage:" + isSlideMessage);
                if (isSlideMessage) {
                    onCancelled();
                    slideShow();
                } else {
                    doAlertForMessageIsNothing();
                }
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
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
    private int getSlideMessageBeanList(String startUid, int length) {
        Log.d("ahokato", "GallerySlideShow#getSlideMessageBeanList startUid:" + startUid + " length:" + length);
        mSlideMessageBeanList = null;
        try {
            mSlideMessageBeanList = SlideMessage.getMessageBeanList(mAccount, mFolder, startUid, length);
            Log.d("ahokato", "GallerySlideShow#getSlideMessageBeanList mSlideMessageBeanList:" + mSlideMessageBeanList.size());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
        }
        return null == mSlideMessageBeanList ? 0 : mSlideMessageBeanList.size();
    }

    //TODO 2012/03/23 add
    private void startNewMailCheckTask() {
        Log.d("ahokato", "GallerySlideShow#startNewMailCheckTask start");
        mNewMailCheckTask = new NewMailCheckTask(mContext);
        if (AsyncTask.Status.RUNNING != mNewMailCheckTask.getStatus()) {
            mNewMailCheckTask.execute();
        }
    }

    //TODO 2012/03/23 add

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class NewMailCheckTask extends AsyncTask<Void, Void, Boolean> implements DialogInterface.OnCancelListener {
        Context context;
        ProgressDialog progressDialog;

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
            Log.d("ahokato", "NewMailCheckTask#doInBackground");
            if (!progressDialog.isIndeterminate()) {
                progressDialog.show();
            }

            //チェック処理開始前に他の処理を止めたり
            mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
            if (mAccount.isAllSync()) {
//                isTmpAllSync = true;
                mAccount.setAllSync(false);
            }
            if (mAccount.isSync()) {
//                isTmpSync = true;
                mAccount.setSync(false);
            }
            isClick = false;

            try {
                int remoteLatestId = MessageSync.getRemoteMessageId(mAccount, mFolder, MessageSync.getHighestRemoteUid(mAccount, mFolder));
                int localLatestId = MessageSync.getRemoteMessageId(mAccount, mFolder, mAccount.getAppRunLatestUid());
                if (localLatestId == remoteLatestId) {
                    Log.d("ahokato", "NewMailCheckTask#doInBackground 新着メールは存在しません");
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
            Log.d("ahokato", "NewMailCheckTask#onPostExecute");
            try {
                String localLatestUid = SlideMessage.getHighestLocalUid(mAccount, mFolder);
                onCancelled();
                if (isNewMail && RakuPhotoStringUtils.isNotBlank(localLatestUid)) {
                    Log.d("ahokato", "NewMailCheckTask#onPostExecute スライドショー対象の新着メール有り 最新メールを表示するよ");
                    updateNewMailCheckLatestInfo(localLatestUid);
                    GallerySlideStop.actionHandle(mContext, mAccount, mFolder, localLatestUid);
                } else {
                    Log.d("ahokato", "NewMailCheckTask#onPostExecute 新着メール無し or スライドショー対象の新着メール無し スライドショーに戻るよ");
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
}
