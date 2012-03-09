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
import android.widget.ImageView;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GalleryDummy extends RakuPhotoActivity implements View.OnClickListener {
    /**
     * mAccount application account
     */
    private static Account mAccount;
    /**
     * folder name
     */
    private static String mFolder;
    /**
     * slide start UID
     */
    private String mSlideStartUid = null;
    private int mLength = 0;
    private int mStart = 0;

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

    private ImageView mImageViewDefault;

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
        Log.d("ahokato", "GalleryDummy#actionSlideShow(index) start");

        Intent intent = new Intent(context, GalleryDummy.class);
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
     * @param icicle Activity起動時に使用
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onCreate(Bundle icicle) {
        Log.d("ahokato", "GalleryDummy#onCreate start");

        super.onCreate(icicle);
        setContentView(R.layout.gallery_dummy);
        onNewIntent(getIntent());

        mImageViewDefault = (ImageView) findViewById(R.id.gallery_dummy_image);
        mImageViewDefault.setOnClickListener(this);
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
        mSlideStartUid = intent.getStringExtra(EXTRA_UID);
        mStart = intent.getIntExtra(EXTRA_START_INDEX, 0);
        mLength = intent.getIntExtra(EXTRA_STOP_INDEX, 0);
        setIntent(intent);
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gallery_dummy_image:
                onSlide();
                break;
            default:
                Log.w(RakuPhotoMail.LOG_TAG, "onClick is no Action !!!!");
        }
    }

    private void onSlide() {
        Log.d("ahokato", "GalleryDummy#onSlide mAccount:" + mAccount.getUuid());
        Log.d("ahokato", "GalleryDummy#onSlide mFolder:" + mFolder);
        Log.d("ahokato", "GalleryDummy#onSlide mStart:" + mStart);
        Log.d("ahokato", "GalleryDummy#onSlide mLength:" + mLength);
        GallerySlideShow.actionSlideShow(this, mAccount, mFolder, mStart, mLength);
    }
}
