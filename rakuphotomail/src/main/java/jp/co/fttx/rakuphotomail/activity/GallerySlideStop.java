/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.internet.BinaryTempFileBody;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideStop extends RakuPhotoActivity implements View.OnClickListener {
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
     *
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd h:mm a";
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
     * mail attachment gallery layout
     */
    private LinearLayout mGalleryLinearLayout;
    /**
     * mail attachment gallery
     */
    private Gallery mGallery;
    /**
     *
     */
    private ProgressDialog mProgressDialog;
    /**
     * user account
     */
    private Account mAccount;
    /**
     *  folder name
     */
    private String mFolder;
    /**
     *  message uid
     */
    private String mUid;

    /**
     * @param context
     * @param account
     * @param folder
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
        try {
            onDisp();
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "GallerySlideStop#onSlide thread Error:" + e);
        }
        Log.d("maguro", "GallerySlideStop#onCreate end");
    }

    private void setupViews() {
        Log.d("maguro", "GallerySlideStop#setupSlideStopViews start");
        mImageViewPicture = (ImageView) findViewById(R.id.gallery_mail_picuture);
        mImageViewPicture.setVisibility(View.VISIBLE);
        mMailSubject = (TextView) findViewById(R.id.gallery_mail_subject);
        mMailDate = (TextView) findViewById(R.id.gallery_mail_date);
        mAnswered = (TextView) findViewById(R.id.gallery_mail_sent_flag);
        mAnswered.setVisibility(View.GONE);
        mMailPre = (TextView) findViewById(R.id.gallery_mail_pre);
        mMailPre.setOnClickListener(this);
        mMailSlide = (TextView) findViewById(R.id.gallery_mail_slide);
        mMailSlide.setOnClickListener(this);
        mMailReply = (TextView) findViewById(R.id.gallery_mail_reply);
        mMailReply.setOnClickListener(this);
        mMailNext = (TextView) findViewById(R.id.gallery_mail_next);
        mMailNext.setOnClickListener(this);
        mGalleryLinearLayout = (LinearLayout) findViewById(R.id.gallery_mail_picture_slide_linear_layoput);
        mGallery = (Gallery) findViewById(R.id.gallery_mail_picture_slide);
        mProgressDialog = new ProgressDialog(this);
        Log.d("maguro", "GallerySlideStop#setupSlideStopViews end");
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

    private void onDisp() throws RakuRakuException {
        MessageBean messageBean = SlideMessage.getMessage(mAccount, mFolder, mUid);
        setImageViewPicture(messageBean.getAttachmentBeanList(), 0);
        mMailSubject.setText(messageBean.getSubject());
        setDate(messageBean.getDate());
        setAnswered(messageBean.isFlagAnswered());
        dissmissProgressDialog();
    }

    private void setImageViewPicture(ArrayList<AttachmentBean> attachmentBeanList, int index){
        Bitmap bitmap = SlideAttachment.getBitmap(mContext, getWindowManager().getDefaultDisplay(), mAccount, attachmentBeanList.get(index));
        mImageViewPicture.setImageBitmap(bitmap);
    }

    private void setDate(long date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        mMailDate.setText(sdf.format(date));
    }

    private void setAnswered(boolean isFlagAnswered){
        if (isFlagAnswered) {
            mAnswered.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        //To change body of implemented methods use File | Settings | File Templates.
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

}
