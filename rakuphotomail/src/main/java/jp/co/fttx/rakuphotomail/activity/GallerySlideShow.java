/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.Attachments;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.MessageInfo;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;

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
     * mesage uid's list
     */
    private ArrayList<String> mUidList = new ArrayList<String>();

    /**
     * Uid List is Repeat?
     */
    private boolean mIsRepeatUidList = true;

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
        onNewIntent(getIntent());

        // TODO ここでUIDを取得しちゃいなよ（新規開始も再開もここ通せよな）
        setUidList();


        Log.d("maguro", "GallerySlideShow#onCreate end");
    }

    /**
     * view setup
     */
    private void setupViews() {
        mSubject = (TextView) findViewById(R.id.gallery_subject);
        mContainer = (ViewGroup) findViewById(R.id.gallery_container);
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
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        Log.d("maguro", "GallerySlideShow#onNewIntent start");
        mAccount = Preferences.getPreferences(this).getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
        mFolder = intent.getStringExtra(EXTRA_FOLDER);
        Log.d("maguro", "GallerySlideShow#onNewIntent end");
    }

    /**
     * get Uid List
     */
    private void setUidList() {
        Log.d("maguro", "GallerySlideShow#setUidList start");
        List<MessageInfo> messageInfoList = SlideMessage.getMessageInfoList(mAccount, mFolder);
        mUidList.clear();
        for (MessageInfo messageInfo : messageInfoList) {
            String uid = messageInfo.getUid();
            Log.d("maguro", "GallerySlideShow#setUidList uid:" + uid);
            ArrayList<Attachments> attachments = SlideMessage.getAttachmentList(mAccount, mFolder, uid);
            if (attachments.size() > 0) {
                mUidList.add(uid);
            }
        }
        Log.d("maguro", "GallerySlideShow#setUidList end");
    }

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
     * slide
     */
    private void onSlide() {
        Log.d("maguro", "GallerySlideShow#onSlide start");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("maguro", "GallerySlideShow#onSlide thread start");
                while (mIsRepeatUidList) {
                    for (Iterator i = mUidList.iterator(); i.hasNext(); ) {
                        Log.d("maguro", "GallerySlideShow#onSlide " + i.next());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onSlide thread:" + e);
                        }
                    }
                }
                Log.d("maguro", "GallerySlideShow#onSlide thread end");
            }
        }).start();
        Log.d("maguro", "GallerySlideShow#onSlide end");
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
                    onSlideStop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.gallery_attachment_picuture_odd:
                try {
                    onSlideStop();
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
    private void onSlideStop() throws InterruptedException {
        Log.d("maguro", "GallerySlideShow#onSlideStop start");
        Log.d("maguro", "GallerySlideShow#onSlideStop end");
    }

}
