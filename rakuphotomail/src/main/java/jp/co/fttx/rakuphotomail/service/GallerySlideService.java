/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideService extends Service {

    public static final String ACTION = "jp.co.fttx.rakuphotomail.service.GallerySlideService.action";

    private final IBinder mBinder = new GallerySlideBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("maguro", "GallerySlideService#onStart");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("maguro", "GallerySlideService#onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("maguro", "GallerySlideService#onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("maguro", "GallerySlideService#onDestroy");
        super.onDestroy();
    }

    public class GallerySlideBinder extends Binder {
        public GallerySlideService getService() {
            Log.d("maguro", "GallerySlideBinder#getService");
            return GallerySlideService.this;
        }
    }

    public void onHoge(){
        Log.d("maguro", "GallerySlideBinder#onHoge start");
        Intent intent = new Intent();
        intent.setAction(ACTION);
        sendBroadcast(intent);
        Log.d("maguro", "GallerySlideBinder#onHoge end");
    }
}
