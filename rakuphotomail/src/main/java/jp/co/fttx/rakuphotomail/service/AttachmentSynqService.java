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
public class AttachmentSynqService extends Service {
	
	private final IBinder mBinder = new AttachmentSynqBinder();
	
	@Override
	public void onCreate() {
		Log.d("dl", "AttachmentSynqService#onCreate");
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("dl", "AttachmentSynqService#onStart");
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("dl", "AttachmentSynqService#onBind");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("dl", "AttachmentSynqService#onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		Log.d("dl", "AttachmentSynqService#onDestroy start");
		super.onDestroy();
		Log.d("dl", "AttachmentSynqService#onDestroy end");
	}
	
	public class AttachmentSynqBinder extends Binder {
		public AttachmentSynqService getService() {
		Log.d("dl", "AttachmentSynqBinder#getService");
		return AttachmentSynqService.this;
		}
	}

}
