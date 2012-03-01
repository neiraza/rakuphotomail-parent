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
import android.os.Bundle;
import android.os.Handler;
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
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideCheck;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
    private ProgressDialog mProgressDialog;
    /**
     * mAccount application account
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
    private Handler mSlideHandler;
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
    private ArrayList<jp.co.fttx.rakuphotomail.mail.Message> mAllMessageList = new ArrayList<jp.co.fttx.rakuphotomail.mail.Message>();
    /**
     * Current Slide message list
     */
    private ArrayList<jp.co.fttx.rakuphotomail.mail.Message> mSlideMessageList = new ArrayList<jp.co.fttx.rakuphotomail.mail.Message>();
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
        Log.d("ahokato", "onCreate start");
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show);
//        setUpProgressDialog(mProgressDialog, getString(R.string.progress_please_wait), getString(R.string.progress_slideshow_server_sync));
        onNewIntent(getIntent());
        setupViews();
    }

//    /**
//     * @param progressDialog progressDialog
//     * @param title          title
//     * @param message        message
//     * @author tooru.oguri
//     * @since rakuphoto 0.1-beta1
//     */
//    private void setUpProgressDialog(ProgressDialog progressDialog, String title, String message) {
//        if (!progressDialog.isShowing()) {
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setTitle(title);
//            progressDialog.setMessage(message);
//            progressDialog.setCancelable(true);
//            progressDialog.show();
//        }
//    }
//
//    /**
//     * @param progressDialog progressDialog
//     * @author tooru.oguri
//     * @since rakuphoto 0.1-beta1
//     */
//    private void dismissProgressDialog(ProgressDialog progressDialog) {
//        if (progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//    }

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
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setImageViewDefault() {
        mImageViewDefault = (ImageView) findViewById(R.id.gallery_attachment_picture_default);
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
        Log.d("ahokato", "onResume start");

        super.onResume();
        try {
            //TODO onCreateでやるべきか、onResumeでやるべきか
            //スライド対象外も含めた全件リスト
            mAllMessageList = MessageSync.getRemoteAllMessage(mAccount, mAccount.getInboxFolderName());
            if (mAllMessageList.isEmpty()) {
                Log.w(RakuPhotoMail.LOG_TAG, getString(R.string.warning_server_no_message));
//                dismissProgressDialog(mProgressDialog);
                dispAlertForNoMessage();
            }
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + getString(R.string.error_rakuraku_exception_message) + e.getMessage());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        }

        try {
            onSlide();
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        }
    }

    private void dispAlertForNoMessage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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
                        //TODO もー1回チェックするとか？
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void onSlide() throws MessagingException {
        Log.d("ahokato", "onSlide start");

        int indexStart = 0;
        //スライド対象UID(n件目〜m件目)リストを作成
        if (null != mSlideStartUid) {
            //TODO Message Class のlistからmSlideStartUidを探さないと
//            indexStart = mAllMessageList.indexOf(mSlideStartUid);
        }
        //TODO 仮に10件ずつチェック同期する
        //TODO 設定時の受信とかをやめさせないと、くだらんデータをDBにつっこんでるぞ
        int indexEnd = 9;
        for (int i = indexStart; i < indexEnd; i++) {
            jp.co.fttx.rakuphotomail.mail.Message message = mAllMessageList.get(i);
            if (MessageSync.isMessage(mAccount, mFolder, message.getUid())) {
                mSlideMessageList.add(message);
            } else {
                if (MessageSync.isSlideRemoteMail(mAccount, mFolder, message)) {
                    mSlideMessageList.add(message);
                }
            }
        }
        mSlideMessageListIndex = 0;
        Log.d("ahokato", "onSlide mSlideMessageList.size():" + mSlideMessageList.size());

        // mSlideMessageListが0件じゃない前提
        mSlideShowLoopRunnable = new Runnable() {
            @Override
            public void run() {
                //終了のお知らせチェック
                if (mSlideMessageList.size() - 1 < mSlideMessageListIndex) {
                    Log.d("ahokato", "onSlide mSlideShowLoopRunnable removeCallbacks");
                    mSlideShowLoopHandler.removeCallbacks(mSlideShowLoopRunnable);
                    finish();
                    //TODO 再帰的に呼び出せばいいのかな（汗
                }

                if (mAttachmentBeanList.size() - 1 < mAttachmentBeanListIndex) {
                    //Fetchしてローカルに置いてDBにつっこめ！
                    jp.co.fttx.rakuphotomail.mail.Message currentMessage = null;
                    try {
                        currentMessage = mSlideMessageList.get(mSlideMessageListIndex);
                        mSlideMessageListIndex += 1;
                        MessageSync.syncMail(mAccount, mFolder, currentMessage);
                        mCurrentMessageBean = SlideMessage.getMessage(mAccount, mFolder, currentMessage.getUid());
                        Log.d("ahokato", "onSlide mSlideShowLoopRunnable UID:" + currentMessage.getUid());
                        mAttachmentBeanListIndex = 0;
                    } catch (MessagingException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
                    } catch (RakuRakuException e) {
                        Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
                    }
                }

                //画像表示処理
                //TODO 再取得する方向は考えなくてよい？
                if (SlideCheck.isDownloadedAttachment(mCurrentMessageBean)) {
                    mAttachmentBeanList = mCurrentMessageBean.getAttachmentBeanList();
                    Log.d("ahokato", "onSlide mAttachmentBeanList.size():" + mAttachmentBeanList.size());
                    Log.d("ahokato", "onSlide mCurrentMessageBean.getUid():" + mCurrentMessageBean.getUid());
                    Log.d("ahokato", "onSlide mCurrentMessageBean.getSubject():" + mCurrentMessageBean.getSubject());

                    AttachmentBean attachmentBean = mAttachmentBeanList.get(mAttachmentBeanListIndex);
                    mAttachmentBeanListIndex += 1;
                    if (SlideCheck.isSlide(attachmentBean)) {
                        try {
                            mBitmap = SlideAttachment.getBitmap(getApplicationContext(), getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
                            if (null == mBitmap) {
                                return;
                            }
                            dispSlide(mCurrentMessageBean);
                            Log.d("ahokato", "onSlide mSlideShowLoopRunnable image:" + attachmentBean.getName());
                        } catch (RakuRakuException e) {
                            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_rakuraku_exception) + e.getMessage());
                        }
                    }
                }

                //次回起動
                mSlideShowLoopHandler.postDelayed(this, mAccount.getSlideSleepTime());
            }
        };
        //初回起動
        mSlideShowLoopHandler.postDelayed(mSlideShowLoopRunnable, 0);
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(final MessageBean messageBean) throws RakuRakuException {
        Log.d("ahokato", "dispSlide start");
        mDispUid = messageBean.getUid();
        mSubject.setText(messageBean.getSubject());
        mSenderName.setText(messageBean.getSenderName());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        mDate.setText(sdf.format(messageBean.getDate()));
        if (messageBean.isFlagAnswered()) {
            mAnswered.setVisibility(View.VISIBLE);
            mAnsweredMark.setVisibility(View.VISIBLE);
        } else {
            mAnswered.setVisibility(View.GONE);
            mAnsweredMark.setVisibility(View.GONE);
        }
        setVisibilityImageView().setImageBitmap(mBitmap);
    }


    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
