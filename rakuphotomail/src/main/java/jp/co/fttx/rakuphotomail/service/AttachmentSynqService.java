/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.service;

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.mail.FetchProfile;
import jp.co.fttx.rakuphotomail.mail.Flag;
import jp.co.fttx.rakuphotomail.mail.Folder;
import jp.co.fttx.rakuphotomail.mail.Folder.OpenMode;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Store;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalFolder;
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

	public static final String ACTION = "action";
	private final IBinder mBinder = new AttachmentSynqBinder();

	@Override
	public void onCreate() {
		Log.d("download_test", "AttachmentSynqService#onCreate");
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("download_test", "AttachmentSynqService#onStart");
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("download_test", "AttachmentSynqService#onBind");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("download_test", "AttachmentSynqService#onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		Log.d("download_test", "AttachmentSynqService#onDestroy start");
		super.onDestroy();
		Log.d("download_test", "AttachmentSynqService#onDestroy end");
	}

	public class AttachmentSynqBinder extends Binder {
		public AttachmentSynqService getService() {
			Log.d("download_test", "AttachmentSynqBinder#getService");
			return AttachmentSynqService.this;
		}
	}
	
	public String fuga(String str){
		return str.concat("test");
	}

	public void download(final Account account, final String folder,
			final String uid) throws MessagingException {
		Log.d("download_test", "AttachmentSynqBinder#download start");
		Folder remoteFolder = null;
		LocalFolder localFolder = null;
		try {
			LocalStore localStore = account.getLocalStore();
			localFolder = localStore.getFolder(folder);
			localFolder.open(OpenMode.READ_WRITE);

			Message message = localFolder.getMessage(uid);

			if (message.isSet(Flag.X_DOWNLOADED_FULL)) {
				Log.d("download_test", "AttachmentSynqBinder#download X_DOWNLOADED_FULL");
				FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.ENVELOPE);
				fp.add(FetchProfile.Item.BODY);
				localFolder.fetch(new Message[] { message }, fp, null);
			} else {
				Log.d("download_test", "AttachmentSynqBinder#download not X_DOWNLOADED_FULL");
				Store remoteStore = account.getRemoteStore();
				remoteFolder = remoteStore.getFolder(folder);
				remoteFolder.open(OpenMode.READ_WRITE);

				Message remoteMessage = remoteFolder.getMessage(uid);
				FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.BODY);
				remoteFolder.fetch(new Message[] { remoteMessage }, fp, null);

				localFolder.appendMessages(new Message[] { remoteMessage });
				fp.add(FetchProfile.Item.ENVELOPE);
				message = localFolder.getMessage(uid);
				localFolder.fetch(new Message[] { message }, fp, null);

				message.setFlag(Flag.X_DOWNLOADED_FULL, true);
			}
			
			Log.d("download_test", "AttachmentSynqBinder#download sendBroadcast");
			Intent views = new Intent();
			views.putExtra("UID", uid);
			views.setAction("action");
			sendBroadcast(views);
			Log.d("download_test", "AttachmentSynqBinder#download end");
		} finally {
			Log.d("download_test", "AttachmentSynqBinder#download finally");
			closeFolder(remoteFolder);
			closeFolder(localFolder);
		}
	}

	private void closeFolder(Folder f) {
		if (f != null) {
			f.close();
		}
	}
}
