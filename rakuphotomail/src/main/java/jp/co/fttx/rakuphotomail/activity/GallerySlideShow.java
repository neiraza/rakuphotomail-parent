/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.ProgressDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.Attachments;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.MessageInfo;
import jp.co.fttx.rakuphotomail.provider.AttachmentProvider;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideCheck;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqReceiver;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
     * Bundle put/get Mail Subject
     */
    private static final String EXTRA_SUBJECT = "subject";

    /**
     * account
     */
    private Account mAccount;
    /**
     * folder name
     */
    private String mFolder;
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
    private AttachmentSynqService mSynqService;
    /**
     * isBound
     */
    private boolean mIsBound = false;
    /**
     * receiver
     */
    private AttachmentSynqReceiver mAttachmentReceiver = new AttachmentSynqReceiver();
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
     * @param context
     * @param account
     * @param folder
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void actionHandleFolder(Context context, Account account, String folder) {
        Log.d("maguro", "GallerySlideShow#actionHandlerFolder start");
        Intent intent = new Intent(context, GallerySlideShow.class);
        if (account != null) {
            intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
        }
        if (folder != null) {
            intent.putExtra(EXTRA_FOLDER, folder);
        }
        context.startActivity(intent);
        Log.d("maguro", "GallerySlideShow#actionHandlerFolder end");
    }

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
        setupViews();
        setUpProgressDialog();
        onNewIntent(getIntent());
        setUidList();
        doBindService();
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
        mSubject = (TextView) findViewById(R.id.gallery_subject);
        mContainer = (ViewGroup) findViewById(R.id.gallery_container);
        mSlideHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.d("maguro", "Handler#handleMessage bitmapをmsg.objから抜いてセットしてみますね");
                setVisibilityImageView().setImageBitmap((Bitmap) msg.obj);
                mSubject.setText(msg.getData().get(EXTRA_SUBJECT).toString());
            }

            ;
        };
        mProgressDialog = new ProgressDialog(this);
        setImageViewDefault();
        setImageViewEven();
        setImageViewOdd();
    }

    /**
     * view setup
     */
    private void setImageViewDefault() {
        mImageViewDefault = (ImageView) findViewById(R.id.gallery_attachment_picuture_default);
        mImageViewDefault.setVisibility(View.VISIBLE);
    }

    /**
     * view setup
     */
    private void setImageViewEven() {
        mImageViewEven = (ImageView) findViewById(R.id.gallery_attachment_picuture_even);
        mImageViewEven.setOnClickListener(this);
        mImageViewEven.setVisibility(View.GONE);
    }

    /**
     * view setup
     */
    private void setImageViewOdd() {
        mImageViewOdd = (ImageView) findViewById(R.id.gallery_attachment_picuture_odd);
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
        intent.setClass(mContext, AttachmentSynqService.class);
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
            IntentFilter attachmenFilter = new IntentFilter(AttachmentSynqService.ACTION);
            registerReceiver(mAttachmentReceiver, attachmenFilter);
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
            Log.d("maguro", "GallerySlideShow#mConnection ServiceConnection#onServiceConnected");
            Log.d("mSynqService", "GallerySlideShow#mConnection ServiceConnection#onServiceConnected");
            mSynqService = ((AttachmentSynqService.AttachmentSynqBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("maguro", "GallerySlideShow#mConnection ServiceConnection#onServiceDisconnected");
            Log.d("mSynqService", "GallerySlideShow#mConnection ServiceConnection#onServiceDisconnected");
            mSynqService = null;
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
                    roopInfinity();
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
     * roooooooop.
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void roopInfinity() throws RakuRakuException {
        while (mIsRepeatUidList) {
            roopUid();
        }
    }

    /**
     * uid roop.
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    // TODO クラス及びメソッド分割をかんがえよっか
    private void roopUid() throws RakuRakuException {
        Log.d("maguro", "GallerySlideShow#roopUid start ");
        for (Iterator i = mUidList.iterator(); i.hasNext(); ) {
            String uid = (String) i.next();
            Log.d("maguro", "GallerySlideShow#roopUid uid:" + uid);
            MessageBean messageBean = SlideMessage.getMessage(mAccount, mFolder, uid);
            Log.d("maguro", "GallerySlideShow#roopUid bean.getId():" + messageBean.getId());
            Log.d("maguro", "GallerySlideShow#roopUid bean.getUid():" + messageBean.getUid());
            Log.d("maguro", "GallerySlideShow#roopUid bean.getSubject():" + messageBean.getSubject());
            Log.d("maguro", "GallerySlideShow#roopUid bean.getAttachmentCount():" + messageBean.getAttachmentCount());
            // TODO デバッグ用なので削除対象
            if (null != messageBean.getAttachmentBeanList() && 0 < messageBean.getAttachmentBeanList().size()) {
                for (AttachmentBean attachmentBean : messageBean.getAttachmentBeanList()) {
                    Log.d("maguro", "GallerySlideShow#roopUid attachmentBean.getId():" + attachmentBean.getId());
                    Log.d("maguro", "GallerySlideShow#roopUid attachmentBean.getMessageId():" + attachmentBean.getMessageId());
                    Log.d("maguro", "GallerySlideShow#roopUid attachmentBean.getMimeType():" + attachmentBean.getMimeType());
                    Log.d("maguro", "GallerySlideShow#roopUid attachmentBean.getName():" + attachmentBean.getName());
                    Log.d("maguro", "GallerySlideShow#roopUid attachmentBean.getSize():" + attachmentBean.getSize());
                    Log.d("maguro", "GallerySlideShow#roopUid attachmentBean.getContentUrl():" + attachmentBean.getContentUrl());
                }
            }

            // ダウソ済みならスライド表示さんへ、そうでない方はダウソさんへ
            if (SlideCheck.isDownloadedAttachment(messageBean)) {
                Log.d("maguro", "GallerySlideShow#roopUid スライド表示しちゃうよー");
                dismissProgressDialog();
                dispSlide(messageBean);
            } else {
                try {
                    Log.d("maguro", "GallerySlideShow#roopUid ダウソしちゃうよー");
                    Log.d("maguro", "GallerySlideShow#roopUid ダウソしちゃうよー mSynqService:" + mSynqService);
                    mSynqService.onDownload(mAccount, mFolder, uid);
                } catch (MessagingException e) {
                    Log.d(RakuPhotoMail.LOG_TAG, "GallerySlideShow#roopUid 次のuid(" + uid + ")をDLしますね");
                }
            }
        }
        Log.d("maguro", "GallerySlideShow#roopUid end");
    }

    /**
     * slide show dispppppppp.
     *
     * @param messageBean
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(MessageBean messageBean) {
        ArrayList<AttachmentBean> attachmentBeanList = messageBean.getAttachmentBeanList();
        for (AttachmentBean attachmentBean : attachmentBeanList) {
            Bitmap bitmap = SlideAttachment.getBitmap(mContext, getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
            if (null == bitmap) {
                Log.d("maguro", "GallerySlideShow#roopUid bitmapがnullなのでreturnしちゃいますね");
                return;
            }
            Message msg = setSendMessage(messageBean);
            msg.obj = bitmap;
            mSlideHandler.sendMessage(msg);
            mDispUid = messageBean.getUid();
            // TODO いったい何分くらい停止するんですかねぇ、あ、でも表示が成功した場合だけですよ？
            sleepSlide(1000L);
        }
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
        // TODO 日付もいれたくね？
        bundle.putString(EXTRA_SUBJECT, messageBean.getSubject());
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
            case R.id.gallery_attachment_picuture_even:
                try {
                    onSlideStop(mDispUid);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.gallery_attachment_picuture_odd:
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
            } catch (InterruptedException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onStop Error:" + e);
                finish();
            }
        }
        Log.d("maguro", "GallerySlideShow#onSlideStop end");
    }

}
