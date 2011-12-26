/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.ProgressDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideCheck;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;
import jp.co.fttx.rakuphotomail.rakuraku.util.ThumbnailImageAdapter;
import jp.co.fttx.rakuphotomail.service.AttachmentSyncReceiver;
import jp.co.fttx.rakuphotomail.service.AttachmentSyncService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideStop extends RakuPhotoActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    /**
     * context
     */
    private Context mContext;
    /**
     * Intent get/put account uuid
     */
    private static final String EXTRA_ACCOUNT = "account";
    /**
     * Intent get/put folder name
     */
    private static final String EXTRA_FOLDER = "folder";
    /**
     * Intent get/put message uid
     */
    private static final String EXTRA_UID = "uid";
    /**
     * Display Receive Date
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd h:mm a";
    /**
     * R.id.gallery_mail_slide
     */
    private static final int ID_GALLERY_MAIL_SLIDE = R.id.gallery_mail_slide;
    /**
     * R.id.gallery_mail_reply
     */
    private static final int ID_GALLERY_MAIL_REPLY = R.id.gallery_mail_reply;
    /**
     * R.id.gallery_mail_pre
     */
    private static final int ID_GALLERY_MAIL_PRE = R.id.gallery_mail_pre;
    /**
     * R.id.gallery_mail_next
     */
    private static final int ID_GALLERY_MAIL_NEXT = R.id.gallery_mail_next;

    /**
     * main image
     */
    private ImageView mImageViewPicture;
    /**
     * mail subject
     */
    private TextView mMailSubject;
    /**
     * mail receive date
     */
    private TextView mMailDate;
    /**
     * mail sent flag
     */
    private TextView mAnswered;
    /**
     * mail pre disp
     */
    private TextView mMailPre;
    /**
     * re slide
     */
    private TextView mMailSlide;
    /**
     * mail next disp
     */
    private TextView mMailNext;
    /**
     * mail reply
     */
    private TextView mMailReply;
    /**
     *
     */
    private ProgressDialog mProgressDialog;
    /**
     * user account
     */
    private Account mAccount;
    /**
     * folder name
     */
    private String mFolder;
    /**
     * message uid
     */
    private String mUid;
    /**
     *
     */
    private MessageBean mMessageBean;
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
     *
     */
    private LinearLayout mGalleryThumbnailInfoLayout;
    /**
     *
     */
    private TextView mGalleryThumbnailInfo;
    /**
     *
     */
    private LinearLayout mGalleryThumbnailLayout;
    /**
     *
     */
    private Gallery mGalleryThumbnail;

    /**
     * @param context
     * @param account
     * @param folder
     * @param uid
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void actionHandle(Context context, Account account, String folder, String uid) {
        Log.d("maguro", "GallerySlideStop#actionHandlerFolder start");
        Intent intent = new Intent(context, GallerySlideStop.class);
        if (null == account || null == folder || uid == null) {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideStop#actionHandle account:" + account + " folder:" + folder + " uid:" + uid);
            // TODO 呼び出し側の対応を後で考える
            return;
        }
        intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
        intent.putExtra(EXTRA_FOLDER, folder);
        intent.putExtra(EXTRA_UID, uid);
        context.startActivity(intent);
        Log.d("maguro", "GallerySlideStop#actionHandlerFolder end");
    }

    /**
     * @param outState
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("maguro", "GallerySlideStop#onSaveInstanceState start");
        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        outState.putString(EXTRA_FOLDER, mFolder);
        outState.putString(EXTRA_UID, mUid);
        Log.d("maguro", "GallerySlideStop#onSaveInstanceState end");
    }

    /**
     * @param savedInstanceState
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("maguro", "GallerySlideStop#onRestoreInstanceState start");
        super.onRestoreInstanceState(savedInstanceState);
        mAccount = Preferences.getPreferences(this)
                .getAccount(savedInstanceState.getString(EXTRA_ACCOUNT));
        mFolder = savedInstanceState.getString(EXTRA_FOLDER);
        mUid = savedInstanceState.getString(EXTRA_UID);
        Log.d("maguro", "GallerySlideStop#onRestoreInstanceState end");
    }

    /**
     * @param savedInstanceState
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("maguro", "GallerySlideStop#onCreate start");
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show_stop);
        setupViews();
        setUpProgressDialog();
        onNewIntent(getIntent());
        setMailMoveVisibility(mUid);
        try {
            onDisp(null);
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideStop#onSlide thread Error:" + e);
        }
        doBindService();
        Log.d("maguro", "GallerySlideStop#onCreate end");
    }

    private void setupViews() {
        Log.d("maguro", "GallerySlideStop#setupSlideStopViews start");
        mImageViewPicture = (ImageView) findViewById(R.id.gallery_mail_picture);
        mImageViewPicture.setVisibility(View.VISIBLE);
        setupViewTopMailInfo();
        setupViewBottomButton();
        mProgressDialog = new ProgressDialog(this);
        setupViewGalleryThumbnail();

        Log.d("maguro", "GallerySlideStop#setupSlideStopViews end");
    }

    private void setupViewTopMailInfo() {
        mMailSubject = (TextView) findViewById(R.id.gallery_mail_subject);
        mMailDate = (TextView) findViewById(R.id.gallery_mail_date);
        mAnswered = (TextView) findViewById(R.id.gallery_mail_sent_flag);
        mAnswered.setVisibility(View.GONE);
    }

    private void setupViewBottomButton() {
        mMailPre = (TextView) findViewById(ID_GALLERY_MAIL_PRE);
        mMailPre.setOnClickListener(this);
        mMailSlide = (TextView) findViewById(ID_GALLERY_MAIL_SLIDE);
        mMailSlide.setOnClickListener(this);
        mMailReply = (TextView) findViewById(ID_GALLERY_MAIL_REPLY);
        mMailReply.setOnClickListener(this);
        mMailNext = (TextView) findViewById(ID_GALLERY_MAIL_NEXT);
        mMailNext.setOnClickListener(this);
    }

    private void setupViewGalleryThumbnail() {
        mGalleryThumbnailInfoLayout = (LinearLayout) findViewById(R.id.gallery_thumbnail_info_layout);
        mGalleryThumbnailInfoLayout.setVisibility(View.GONE);
        mGalleryThumbnailInfo = (TextView) findViewById(R.id.gallery_thumbnail_info);
        mGalleryThumbnailInfo.setOnClickListener(this);
        mGalleryThumbnailLayout = (LinearLayout) findViewById(R.id.gallery_thumbnail_layout);
        mGalleryThumbnailLayout.setVisibility(View.GONE);
        mGalleryThumbnail = (Gallery) findViewById(R.id.gallery_thumbnail);
        mGalleryThumbnail.setOnItemClickListener((AdapterView.OnItemClickListener) mContext);
    }

    /**
     * @param intent
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onNewIntent(Intent intent) {
        Log.d("maguro", "GallerySlideStop#onNewIntent start");
        mAccount = Preferences.getPreferences(this).getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
        mFolder = intent.getStringExtra(EXTRA_FOLDER);
        mUid = intent.getStringExtra(EXTRA_UID);
        Log.d("maguro", "GallerySlideStop#onNewIntent end");
    }

    private void onDisp(MessageBean messageBean) throws RakuRakuException {
        Log.d("maguro", "GallerySlideStop#onDisp start");
        if (null == messageBean) {
            messageBean = SlideMessage.getMessage(mAccount, mFolder, mUid);
        }
        mMessageBean = messageBean;
        //Thumbnail
        ArrayList<AttachmentBean> attachmentBeanList = messageBean.getAttachmentBeanList();
        if (1 < attachmentBeanList.size()) {
            mGalleryThumbnailLayout.setVisibility(View.VISIBLE);
            ThumbnailImageAdapter thumbnailAdapter = new ThumbnailImageAdapter(mContext);
            thumbnailAdapter.setImageItems(makeBitmapList(attachmentBeanList));
            mGalleryThumbnail.setAdapter(thumbnailAdapter);
        } else {
            mGalleryThumbnailLayout.setVisibility(View.GONE);
        }

        //ここはThumnailから選択できるように変更する予定
        setImageViewPicture(messageBean.getAttachmentBeanList(), 0);

        mMailSubject.setText(messageBean.getSubject());
        setDate(messageBean.getDate());
        setAnswered(messageBean.isFlagAnswered());
        dissmissProgressDialog();
        Log.d("maguro", "GallerySlideStop#onDisp end");
    }

    private ArrayList<Bitmap> makeBitmapList(ArrayList<AttachmentBean> beanArrayList) {
        Log.d("maguro", "GallerySlideStop#makeBitmapList");
        ArrayList<Bitmap> resultList = new ArrayList<Bitmap>();
        for (AttachmentBean attachmentBean : beanArrayList) {
            resultList.add(getThumbnailBitmap(attachmentBean));
        }
        return resultList;
    }

    private Bitmap getThumbnailBitmap(AttachmentBean attachmentBean) {
        return SlideAttachment.getThumbnailBitmap(mContext, getWindowManager().getDefaultDisplay(), mAccount, attachmentBean);
    }

    private void setImageViewPicture(ArrayList<AttachmentBean> attachmentBeanList, int index) {
        Bitmap bitmap = SlideAttachment.getBitmap(mContext, getWindowManager().getDefaultDisplay(), mAccount, attachmentBeanList.get(index));
        mImageViewPicture.setImageBitmap(bitmap);
    }

    private void setDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        mMailDate.setText(sdf.format(date));
    }

    private void setAnswered(boolean isFlagAnswered) {
        if (isFlagAnswered) {
            mAnswered.setVisibility(View.VISIBLE);
        }
    }

    private void setUpProgressDialog() {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("処理中なんですが？");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }
    }

    private void dissmissProgressDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        Log.d("maguro", "GallerySlideStop#onStop start");
        super.onStop();

        Log.d("maguro", "GallerySlideStop#onStop end");
    }

    @Override
    public void onDestroy() {
        Log.d("maguro", "GallerySlideStop#onDestroy start");
        super.onDestroy();
        doUnbindService();
        finish();
        Log.d("maguro", "GallerySlideStop#onDestroy end");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_GALLERY_MAIL_SLIDE:
                onSlide();
                break;
            case ID_GALLERY_MAIL_REPLY:
                onReply();
                break;
            case ID_GALLERY_MAIL_PRE:
                onMailPre();
                break;
            case ID_GALLERY_MAIL_NEXT:
                onMailNext();
                break;
            default:
                Log.w(RakuPhotoMail.LOG_TAG, "onClick is no Action !!!!");
        }
    }

    private void onSlide() {
        Log.d("maguro", "GallerySlideStop#onSlide");
        GallerySlideShow.actionSlideShow(this, mAccount, mFolder, mMessageBean.getUid());
    }

    private void onReply() {
        Log.d("maguro", "GallerySlideStop#onReply");
        if (null != mMessageBean) {
            GallerySendingMail.actionReply(this, mMessageBean);
        } else {
            Toast.makeText(GallerySlideStop.this, "メールが存在しません。", Toast.LENGTH_SHORT);
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideStop#onReply() メールが存在しません UID:" + mUid);
        }
    }

    private void onMailNext() {
        Log.d("maguro", "GallerySlideStop#onMailNext");
        try {
            MessageBean messageBean = SlideMessage.getNextMessage(mAccount, mFolder, mMessageBean.getUid());
            dispSlide(messageBean);
            Log.d("maguro", "GallerySlideStop#onMailNext messageBean.getUid():" + messageBean.getUid());
            setMailMoveVisibility(messageBean.getUid());
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideStop#onMailNext() 次のメールが取得できず UID:" + mMessageBean.getUid());
        }
    }

    private void onMailPre() {
        Log.d("maguro", "GallerySlideStop#onMailPre");
        try {
            MessageBean messageBean = SlideMessage.getPreMessage(mAccount, mFolder, mMessageBean.getUid());
            dispSlide(messageBean);
            Log.d("maguro", "GallerySlideStop#onMailNext messageBean.getUid():" + messageBean.getUid());
            setMailMoveVisibility(messageBean.getUid());
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideStop#onMailPre() 前のメールが取得できず UID:" + mMessageBean.getUid());
        }
    }

    /**
     * @param messageBean MessageBean
     * @throws RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dispSlide(MessageBean messageBean) throws RakuRakuException {
        Log.d("maguro", "GallerySlideStop#dispSlide(String) start");
        if (SlideCheck.isDownloadedAttachment(messageBean)) {
            //画像以外は先に共通で表示しちゃおうか
            onDisp(messageBean);
        } else {
            try {
                if (null == mSyncService) {
                    Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideStop#loopUid mSyncServiceがnullでした。");
                    return;
                }
                mSyncService.onDownload(mAccount, mFolder, messageBean.getUid(), AttachmentSyncService.ACTION_SLIDE_SHOW_STOP);
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideStop#loopUid なぜかError!!!!");
            }
        }
        Log.d("maguro", "GallerySlideStop#dispSlide(String) end");
    }

    private void setMailMoveVisibility(String uid) {
        if (!SlideMessage.isNextMessage(mAccount, mFolder, uid)) {
            mMailNext.setVisibility(View.GONE);
        } else {
            mMailNext.setVisibility(View.VISIBLE);
        }
        if (!SlideMessage.isPreMessage(mAccount, mFolder, uid)) {
            mMailPre.setVisibility(View.GONE);
        } else {
            mMailPre.setVisibility(View.VISIBLE);
        }
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void doBindService() {
        Log.d("maguro", "GallerySlideStop#doBindService start");
        if (!mIsBound) {
            mIsBound = bindService(getIntent(), mConnection, Context.BIND_AUTO_CREATE);
            IntentFilter attachmentFilter = new IntentFilter(AttachmentSyncService.ACTION_SLIDE_SHOW_STOP);
            registerReceiver(mAttachmentReceiver, attachmentFilter);
        }
        Log.d("maguro", "GallerySlideStop#doBindService end");
    }

    private void doUnbindService() {
        Log.d("maguro", "GallerySlideStop#doUnBindService start");
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
            unregisterReceiver(mAttachmentReceiver);
        }
        Log.d("maguro", "GallerySlideStop#doUnBindService end");
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("maguro", "GallerySlideStop#onItemClick id:" + id);
    }
}
