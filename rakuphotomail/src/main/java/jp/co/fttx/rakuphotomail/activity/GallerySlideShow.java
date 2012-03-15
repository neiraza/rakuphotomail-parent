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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
     * All message list
     */
//    private ArrayList<jp.co.fttx.rakuphotomail.mail.Message> mAllMessageList = new ArrayList<jp.co.fttx.rakuphotomail.mail.Message>();
    /**
     * Current Slide message list
     */
//    private ArrayList<jp.co.fttx.rakuphotomail.mail.Message> mSlideMessageList = new ArrayList<jp.co.fttx.rakuphotomail.mail.Message>();
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
     * Handler
     */
    private Handler mNewMailCheckHandler = new Handler();
    /**
     *
     */
    private Runnable mSlideShowLoopRunnable;
    /**
     *
     */
    private Runnable mNewMailCheckRunnable;
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

    private static int mStart = 0;
    private static int mEnd = 0;
    private static final int LENGTH = 100;

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
        if (mAccount.isAllSync()) {
            int tmpStart = mAccount.getCheckStartId();
            int tmpEnd = mAccount.getCheckEndId();
            // チェック処理中断から再開へ
            if (0 != tmpStart && 0 != tmpEnd) {
                Log.d("ahokato", "GallerySlideShow#onCreate 中断から再開");
                mStart = tmpStart;
                mEnd = tmpEnd;
            } else {
                Log.d("ahokato", "GallerySlideShow#onCreate 初めてなので全部");
                try {
                    mEnd = MessageSync.getRemoteMessageCount(mAccount, mFolder);
                    mAccount.setLocalLatestId(mEnd);
                    mStart = mEnd - LENGTH;
                    if (0 >= mStart) {
                        mStart = 1;
                    }
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                }
            }
            Log.d("ahokato", "GallerySlideShow#onCreate mStart:" + mStart);
            Log.d("ahokato", "GallerySlideShow#onCreate mEnd:" + mEnd);
        } else {
            Log.d("ahokato", "GallerySlideShow#onCreate 以前の話だが、syncはもう全部やったのだよ");
            try {
                String highestLocalUid = MessageSync.getUid(mAccount, mFolder, mAccount.getLocalLatestId());
                String highestRemoteUid = MessageSync.getHighestRemoteUid(mAccount, mAccount.getInboxFolderName());
                Log.d("ahokato", "GallerySlideShow#onCreate highestLocalUid:" + highestLocalUid);
                Log.d("ahokato", "GallerySlideShow#onCreate highestRemoteUid:" + highestRemoteUid);

                if (RakuPhotoStringUtils.isNotBlank(highestRemoteUid, highestLocalUid)) {
                    if (highestLocalUid.equals(highestRemoteUid)) {
                        Log.d("ahokato", "GallerySlideShow#onCreate Localが最新のようだ");
                        //TODO ここがスライド用のBeanを生成しているところ
                        getSlideMessageBeanList();
                    } else {
                        Log.d("ahokato", "GallerySlideShow#onCreate Localよりも新しいやつがRemoteにいるぞ");
                        //ここを過去の原点とし、ここまではチェックするぜの証
                        mAccount.setLocalOldId(mAccount.getLocalLatestId());
                        //ここを原点にする
                        mEnd = MessageSync.getRemoteMessageCount(mAccount, mFolder);
                        mAccount.setLocalLatestId(mEnd);

                        mStart = mEnd - LENGTH;
                        if (0 >= mStart) {
                            mStart = mAccount.getLocalOldId();
                        }

                        mAccount.setSync(true);
                        Log.d("ahokato", "GallerySlideShow#onCreate mStart:" + mStart);
                        Log.d("ahokato", "GallerySlideShow#onCreate mEnd:" + mEnd);
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
        mStart = intent.getIntExtra(EXTRA_START_INDEX, 0);
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
        Log.d("ahokato", "GallerySlideShow#onResume start");
        super.onResume();
        if (isOptionMenu) {
            actionSlideShow(mContext, mAccount, mFolder, mDispUid);
            finish();
        } else {
            //TODO 新着メールチェックさん
//                onNewMailCheck();
            mMessageSyncTask = null;
            if (mAccount.isAllSync() || mAccount.isSync()) {
                startMessageSyncTask();
            } else {
                try {
                    slideShow();
                } catch (MessagingException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                }
            }
        }
    }

    private void dispAlertForNoMessage() {
        Log.d("ahokato", "GallerySlideShow#dispAlertForNoMessage start");
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
        Log.d("ahokato", "GallerySlideShow#dispAlertForNoMessage end");
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
                    removeCache();
                    Log.d("ahokato", "GallerySlideShow#slideShow removeCallbacks");
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
                            mCurrentMessageBean = mSlideMessageBeanList.get(mSlideMessageListIndex);
                            mSlideMessageListIndex += 1;
                            mAttachmentBeanListIndex = 0;
                        } else {
                            // 再ダウンロード確認＆希望
                            Log.d("ahokato", "GallerySlideShow#slideShow 再ダウンロード確認&希望");
                            mCurrentMessageBean = getSyncMessage(mCurrentMessageBean);
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
        Log.d("ahokato", "GallerySlideShow#dispSlide(MessageBean messageBean) start");
        if (MessageSync.isMessage(mAccount, mFolder, messageBean.getUid()) && !SlideCheck.isDownloadedAttachment(messageBean)) {
            Log.d("ahokato", "GallerySlideShow#dispSlide(MessageBean messageBean) 添付ファイル同期しまーす");
            MessageSync.syncMailForAttachmentDownload(mAccount, mFolder, messageBean);
        }
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

    private void doSort(List list, Comparator comparator) {
        Collections.sort(list, comparator);
    }

    private void removeCache() {
        Log.d("ahokato", "GallerySlideShow#removeCache start");
        try {
            MessageSync.removeCache(mAccount, mFolder, mDispUid);
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        }
    }

//    /**
//     * @author tooru.oguri
//     * @since rakuphoto 0.1-beta1
//     */
//    private void onNewMailCheck() throws MessagingException {
//        Log.d("ahokato", "GallerySlideShow#onCheck start");
//        mNewMailCheckRunnable = new Runnable() {
//            @Override
//            public void run() {
//                String newUid = null;
//                ArrayList<String> newUidList = new ArrayList<String>();
//                try {
//                    ArrayList<jp.co.fttx.rakuphotomail.mail.Message> newAllMessageList = MessageSync.getRemoteAllMessage(mAccount, mAccount.getInboxFolderName());
//                    newUidList = RakuPhotoListUtil.getNewUidList(mAllMessageList, newAllMessageList);
//                    newUid = RakuPhotoListUtil.getNewUid(mAccount, mFolder, newUidList, newAllMessageList);
//                } catch (MessagingException e) {
//                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
//                } catch (RakuRakuException e) {
//                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + getString(R.string.error_rakuraku_exception_message) + e.getMessage());
//                }
//
//                try {
//                    for (String uid : newUidList) {
//                        MessageSync.syncMail(mAccount, mFolder, uid);
//                    }
//                } catch (MessagingException e) {
//                    Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
//                }
//
//                if (null != newUid) {
//                    mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
//                    mNewMailCheckHandler.removeCallbacks(mNewMailCheckRunnable);
//                    GallerySlideStop.actionHandle(mContext, mAccount, mFolder, newUid);
//                    finish();
//                }
//
//                //次回起動
//                Log.d("ahokato", "GallerySlideShow#onCheck 次回起動");
//                mNewMailCheckHandler.postDelayed(this, mAccount.getServerSyncTimeDuration());
//            }
//
//        };
//
//        //初回起動
//        Log.d("ahokato", "GallerySlideShow#onCheck 初回起動");
//        mNewMailCheckHandler.postDelayed(mNewMailCheckRunnable, mAccount.getServerSyncTimeDuration());
//    }

    @Override
    public void onStop() {
        Log.d("ahokato", "GallerySlideShow#onStop start");
        super.onStop();
        mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
        mNewMailCheckHandler.removeCallbacks(mNewMailCheckRunnable);
        if (null != mMessageSyncTask) {
            mMessageSyncTask.cancel(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
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

    /**
     * event slide stop
     *
     * @param uid message uid
     * @throws InterruptedException InterruptedException
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void onSlideStop(String uid) throws InterruptedException {
//        startProgressDialogHandler(getString(R.string.progress_please_wait), getString(R.string.progress_slideshow_stop));
        mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
        mNewMailCheckHandler.removeCallbacks(mNewMailCheckRunnable);
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

    private MessageSyncTask mMessageSyncTask;

    private void startMessageSyncTask() {
        Log.d("ahokato", "GallerySlideShow#startMessageSyncTask start");
        mMessageSyncTask = new MessageSyncTask(mContext);
        mMessageSyncTask.execute();
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class MessageSyncTask extends AsyncTask<Void, Void, Void> implements DialogInterface.OnCancelListener {
        Context context;
        ProgressDialog progressDialog;

        public MessageSyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(getString(R.string.progress_please_wait));
            progressDialog.setMessage("メールサーバーをチェックしています(" + mStart + "~" + mEnd + ")");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(this);
            progressDialog.show();
        }

        /**
         * @return null
         */
        @Override
        protected Void doInBackground(Void... params) {
            if (!progressDialog.isIndeterminate()) {
                progressDialog.show();
            }
            Log.d("ahokato", "MessageSyncTask#doInBackground mStart:" + mStart);
            Log.d("ahokato", "MessageSyncTask#doInBackground mEnd:" + mEnd);

            Log.d("ahokato", "MessageSyncTask#doInBackground syncしまーす");
            MessageSync.syncMailbox(mAccount, mFolder, mStart, mEnd);
            // Account に mStart & mEnd !!
            mAccount.setCheckStartId(mStart);
            mAccount.setCheckEndId(mEnd);
            Log.d("ahokato", "MessageSyncTask#doInBackground syncしましたー");

            if (mAccount.isAllSync()) {
                if (1 != mStart) {
                    mEnd = mStart - 1;
                    if (mEnd <= 0) {
                        mEnd = 1;
                    }
                    mStart = mStart - LENGTH;
                    if (mStart <= 0) {
                        mStart = 1;
                    }
                } else {
                    clearCount();
                }
            } else if (mAccount.isSync()) {
                if (mAccount.getLocalOldId() != mStart) {
                    mEnd = mStart - 1;
                    if (mEnd <= mAccount.getLocalOldId()) {
                        mEnd = mAccount.getLocalOldId();
                    }
                    mStart = mStart - LENGTH;
                    if (mStart <= mAccount.getLocalOldId()) {
                        mStart = mAccount.getLocalOldId();
                    }
                } else {
                    clearCount();
                }
            }

            Log.d("ahokato", "MessageSyncTask#doInBackground mStart:" + mStart);
            Log.d("ahokato", "MessageSyncTask#doInBackground mEnd:" + mEnd);
            //TODO ここがスライド用のBeanを生成しているところ
            getSlideMessageBeanList();
            return null;
        }

        private void clearCount() {
            mAccount.setAllSync(false);
            mAccount.setSync(false);
            mStart = 0;
            mAccount.setCheckStartId(mStart);
            mEnd = 0;
            mAccount.setCheckEndId(mEnd);
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
        protected void onPostExecute(Void param) {
            try {
                Log.d("ahokato", "MessageSyncTask#onPostExecute");
                onCancelled();
                slideShow();
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    private void getSlideMessageBeanList() {
        Log.d("ahokato", "GallerySlideShow#getSlideMessageBeanList start");

        try {
            mSlideMessageBeanList = null;
            try {
                mSlideMessageBeanList = SlideMessage.getMessageBeanList(mAccount, mFolder);
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            }
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
        }
    }
}
