/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.ProgressDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import jp.co.fttx.rakuphotomail.service.AttachmentSyncReceiver;
import jp.co.fttx.rakuphotomail.service.AttachmentSyncService;

import java.text.SimpleDateFormat;
import java.util.*;

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
    private static final String MESSAGE_ANSWERED = "answered";
    /**
     * R.id.gallery_attachment_picuture_default
     */
    private static final int ID_GALLERY_ATTACHMENT_PICTURE_DEFAULT = R.id.gallery_attachment_picuture_default;
    /**
     * R.id.gallery_attachment_picuture_even
     */
    private static final int ID_GALLERY_ATTACHMENT_PICTURE_EVEN = R.id.gallery_attachment_picuture_even;
    /**
     * R.id.gallery_attachment_picuture_odd
     */
    private static final int ID_GALLERY_ATTACHMENT_PICTURE_ODD = R.id.gallery_attachment_picuture_odd;

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
     * view layout container
     */
    private ViewGroup mContainer;
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
    private ArrayList<String> mUidList = new ArrayList<String>();
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
     * @param context
     * @param account
     * @param folder
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void actionSlideShow(Context context, Account account, String folder, String uid) {
        Log.d("maguro", "GallerySlideShow#actionSlideShow start");
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
        Log.d("maguro", "GallerySlideShow#actionSlideShow end");
    }

    //TODO Timer TEST
    Timer mTimer   = null;


    /**
     * @param savedInstanceState
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("maguro", "GallerySlideShow#onCreate start");
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show);
        mProgressDialog = new ProgressDialog(this);
        setUpProgressDialog();
        onNewIntent(getIntent());
        setupViews();
        setUidList();
        doBindService();

        //TODO Timer TEST
        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run() {
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                MessageSync.synchronizeMailbox(mAccount,mFolder);
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
                Log.d("maguro", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
            }
        }, 60000L, 60000L);

        Log.d("maguro", "GallerySlideShow#onCreate end");
    }

    private void setUpProgressDialog() {
        Log.d("maguro", "GallerySlideShow#setUpProgressDialog start");
        if (!mProgressDialog.isShowing()) {
            Log.d("maguro", "GallerySlideShow#setUpProgressDialog 時間よ止まれ");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("処理中なんですが？");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }
        Log.d("maguro", "GallerySlideShow#setUpProgressDialog end");
    }

    private void dismissProgressDialog() {
        Log.d("maguro", "GallerySlideShow#dissmissProgressDialog start");
        if (mProgressDialog.isShowing()) {
            Log.d("maguro", "GallerySlideShow#setUpProgressDialog 時間よ動け");
            mProgressDialog.dismiss();
        }
        Log.d("maguro", "GallerySlideShow#dissmissProgressDialog end");
    }

    /**
     * view setup
     */
    private void setupViews() {
        setImageViewDefault();
        setImageViewEven();
        setImageViewOdd();
        mSubject = (TextView) findViewById(R.id.gallery_subject);
        mContainer = (ViewGroup) findViewById(R.id.gallery_container);
        mSlideHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.d("maguro", "Handler#handleMessage bitmapをmsg.objから抜いてセットしてみますね");
                setVisibilityImageView().setImageBitmap((Bitmap) msg.obj);
                Bundle bundle = msg.getData();
                mSubject.setText(bundle.get(MESSAGE_SUBJECT).toString());
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                mDate.setText(sdf.format(bundle.get(MESSAGE_DATE)));
                if ((Boolean) bundle.get(MESSAGE_ANSWERED)) {
                    mAnswered.setVisibility(View.VISIBLE);
                }
            }

            ;
        };
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
            Log.d("maguro", "Handler#setImageViewDefault VISIBLE mStartUid:" + mStartUid);
            mImageViewDefault.setVisibility(View.VISIBLE);
        } else {
            Log.d("maguro", "Handler#setImageViewDefault GONE mStartUid:" + mStartUid);
            mImageViewDefault.setVisibility(View.GONE);
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
     * @param intent
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onNewIntent(Intent intent) {
        Log.d("maguro", "GallerySlideShow#onNewIntent start");
        mAccount = Preferences.getPreferences(this).getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
        mFolder = intent.getStringExtra(EXTRA_FOLDER);
        mStartUid = intent.getStringExtra(EXTRA_UID);
        intent.setClass(mContext, AttachmentSyncService.class);
        setIntent(intent);
        Log.d("maguro", "GallerySlideShow#onNewIntent end");
    }

    /**
     * get Uid List
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setUidList() {
        Log.d("maguro", "GallerySlideShow#setUidList start");
        List<MessageInfo> messageInfoList = SlideMessage.getMessageInfoList(mAccount, mFolder);
        mUidList.clear();
        for (MessageInfo messageInfo : messageInfoList) {
            String uid = messageInfo.getUid();
            Log.d("maguro", "GallerySlideShow#setUidList uid:" + uid);
            ArrayList<Attachments> attachmentsList = SlideMessage.getAttachmentList(mAccount, mFolder, uid);
            for (Attachments attachments : attachmentsList) {
                //追加対象はスライドする奴のみ（重複UIDは省く）
                if (SlideCheck.isSlide(attachments) && !mUidList.contains(uid)) {
                    Log.d("maguro", "GallerySlideShow#setUidList mUidListにuid(" + uid + ")を追加しますた");
                    mUidList.add(uid);
                }
            }
        }
        Log.d("maguro", "GallerySlideShow#setUidList end");
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void doBindService() {
        Log.d("maguro", "GallerySlideShow#doBindService start");
        if (!mIsBound) {
            mIsBound = bindService(getIntent(), mConnection, Context.BIND_AUTO_CREATE);
            IntentFilter attachmentFilter = new IntentFilter(AttachmentSyncService.ACTION_SLIDE_SHOW);
            registerReceiver(mAttachmentReceiver, attachmentFilter);
        }
        Log.d("maguro", "GallerySlideShow#doBindService end");
    }

    private void doUnbindService() {
        Log.d("maguro", "GallerySlideShow#doUnBindService start");
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
            unregisterReceiver(mAttachmentReceiver);
        }
        Log.d("maguro", "GallerySlideShow#doUnBindService end");
    }

    /**
     * ServiceConnection
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSyncService = ((AttachmentSyncService.AttachmentSyncBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSyncService = null;
        }
    };

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onResume() {
        Log.d("maguro", "GallerySlideShow#onResume start");
        super.onResume();
        if (null != mUidList && 0 < mUidList.size()) {
            onSlide();
        }
        Log.d("maguro", "GallerySlideShow#onResume end");
    }

    /**
     * slide.
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void onSlide() {
        Log.d("maguro", "GallerySlideShow#onSlide start");
        mSlideShowThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("maguro", "GallerySlideShow#onSlide thread start");
                try {
                    loopInfinite();
                } catch (RakuRakuException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onSlide thread Error:" + e);
                }
                Log.d("maguro", "GallerySlideShow#onSlide thread end");
            }
        });
        mSlideShowThread.start();
        Log.d("maguro", "GallerySlideShow#onSlide end");
    }

    /**
     * loooooooop.
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void loopInfinite() throws RakuRakuException {
        Log.d("maguro", "GallerySlideShow#loopInfinite start ");
        while (mIsRepeatUidList) {
            if ("".equals(mStartUid)) {
                loop();
            }
            loopNumbered();
        }
        Log.d("maguro", "GallerySlideShow#loopInfinite end ");
    }

    /**
     * uid loop.
     *
     * @throws RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void loop() throws RakuRakuException {
        Log.d("maguro", "GallerySlideShow#loop start ");
        for (Iterator i = mUidList.iterator(); i.hasNext(); ) {
            dispSlide((String) i.next());
        }
        Log.d("maguro", "GallerySlideShow#loop end");
    }

    /**
     * uid specified.
     *
     * @throws RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta
     */
    private void loopNumbered() throws RakuRakuException {
        boolean reset = false;
        for (Iterator i = mUidList.iterator(); i.hasNext(); ) {
            Log.d("maguro", "GallerySlideShow#loopNumbered start");
            String uid = (String) i.next();
            if (mStartUid.equals(uid)) {
                Log.d("maguro", "GallerySlideShow#loopNumbered 一時停止した見つけた uid:" + uid);
                reset = true;
                mStartUid = "";
            }
            if (reset) {
                dismissProgressDialog();
                dispSlide(uid);
            }
        }
        Log.d("maguro", "GallerySlideShow#loopNumbered end");
    }

    /**
     * @param uid mail uid
     * @throws RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(String uid) throws RakuRakuException {
        Log.d("maguro", "GallerySlideShow#dispSlide(String) start");
        MessageBean messageBean = SlideMessage.getMessage(mAccount, mFolder, uid);
        if (SlideCheck.isDownloadedAttachment(messageBean)) {
            dismissProgressDialog();
            dispSlide(messageBean);
        } else {
            try {
                if (null == mSyncService) {
                    Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#loopUid mSyncServiceがnullでした。");
                    return;
                }
                mSyncService.onDownload(mAccount, mFolder, uid, AttachmentSyncService.ACTION_SLIDE_SHOW);
            } catch (MessagingException e) {
                Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#loopUid 次のuid(" + uid + ")をDLしますね");
            }
        }
        Log.d("maguro", "GallerySlideShow#dispSlide(String) end");
    }

    /**
     * slide show dispppppppp.
     *
     * @param messageBean
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(MessageBean messageBean) {
        Log.d("maguro", "GallerySlideShow#dispSlide(MessageBean) start");
        ArrayList<AttachmentBean> attachmentBeanList = messageBean.getAttachmentBeanList();
        for (AttachmentBean attachmentBean : attachmentBeanList) {
            Bitmap bitmap = SlideAttachment.getBitmap(mContext, getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
            if (null == bitmap) {
                Log.d("maguro", "GallerySlideShow#loopUid bitmapがnullなのでreturnしちゃいますね");
                return;
            }
            Message msg = setSendMessage(messageBean);
            msg.obj = bitmap;
            mSlideHandler.sendMessage(msg);
            mDispUid = messageBean.getUid();
            // TODO いったい何分くらい停止するんですかねぇ、あ、でも表示が成功した場合だけですよ？
            sleepSlide(1000L);
        }
        Log.d("maguro", "GallerySlideShow#dispSlide(MessageBean) end");
    }

    /**
     * @param messageBean
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
     * @param sleepTime
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void sleepSlide(long sleepTime) {
        Log.d("maguro", "GallerySlideShow#sleepSlide start");
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#stopSlide error:" + e);
        }
        Log.d("maguro", "GallerySlideShow#sleepSlide end");
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private ImageView setVisibilityImageView() {
        Log.d("maguro", "GallerySlideShow#setVisibilityImageView");
        ImageView imageView = null;
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
        return imageView;
    }

    @Override
    public void onStop() {
        Log.d("maguro", "GallerySlideShow#onStop start");
        super.onStop();
        try {
            doUnbindService();
            mIsRepeatUidList = false;
            mSlideShowThread.join();
        } catch (InterruptedException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onStop Error:" + e);
            finish();
        }
        Log.d("maguro", "GallerySlideShow#onStop stop");
    }

    @Override
    public void onDestroy() {
        Log.d("maguro", "GallerySlideShow#onDestroy start");
        super.onDestroy();
        mTimer.cancel();
        mTimer = null;
        Log.d("maguro", "GallerySlideShow#onDestroy stop");
    }

    /**
     * @param outState
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("maguro", "GallerySlideShow#onSaveInstanceState start");
        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        outState.putString(EXTRA_FOLDER, mFolder);
        outState.putString(EXTRA_UID, mStartUid);
        Log.d("maguro", "GallerySlideShow#onSaveInstanceState end");
    }

    /**
     * @param savedInstanceState
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("maguro", "GallerySlideShow#onRestoreInstanceState start");
        super.onRestoreInstanceState(savedInstanceState);
        mAccount = Preferences.getPreferences(this)
                .getAccount(savedInstanceState.getString(EXTRA_ACCOUNT));
        mFolder = savedInstanceState.getString(EXTRA_FOLDER);
        mStartUid = savedInstanceState.getString(EXTRA_UID);
        Log.d("maguro", "GallerySlideShow#onRestoreInstanceState end");
    }

    /**
     * @param v
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
     * @throws InterruptedException
     */
    private void onSlideStop(String uid) throws InterruptedException {
        Log.d("maguro", "GallerySlideShow#onSlideStop start");
        if (null != mDispUid) {
            try {
                doUnbindService();
                mIsRepeatUidList = false;
                mSlideShowThread.join();
                GallerySlideStop.actionHandle(mContext, mAccount, mFolder, uid);
                finish();
            } catch (InterruptedException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onStop Error:" + e);
                finish();
            }
        }
        Log.d("maguro", "GallerySlideShow#onSlideStop end");
    }

}
