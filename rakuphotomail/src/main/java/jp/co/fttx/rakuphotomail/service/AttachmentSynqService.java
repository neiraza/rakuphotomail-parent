/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.service;

import java.util.ArrayList;
import java.util.List;

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

    public static final String ACTION = "jp.co.fttx.rakuphotomail.service.AttachmentSynqService.action";

    private final IBinder mBinder = new AttachmentSynqBinder();
    private List<String> downloadList;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadList = new ArrayList<String>();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("maguro", "AttachmentSynqService#onStart");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("maguro", "AttachmentSynqService#onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("maguro", "AttachmentSynqService#onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("maguro", "AttachmentSynqService#onDestroy");
        super.onDestroy();
    }

    public class AttachmentSynqBinder extends Binder {
        public AttachmentSynqService getService() {
            Log.d("maguro", "AttachmentSynqBinder#getService");
            return AttachmentSynqService.this;
        }
    }

    /**
     * <p>
     * ダウンロード処理.
     * </p>
     * <p>
     * downloadListに含まれていないuidのメールのみ、ダウンロードの対象とする。<br>
     * これはスライドショーの中で、ダウンロードリクエストが非同期に送信される前提で、<br>
     * 同じリクエストが複数回無駄に送信される事を避けるため。<br>
     * 
     * @param account
     *            アカウント情報
     * @param folder
     *            フォルダー名
     * @param uid
     *            メール識別番号
     * @throws MessagingException
     */
    public void onDownload(final Account account, final String folder, final String uid)
                    throws MessagingException {
        Log.d("maguro", "AttachmentSynqService#onDownload start");
        Log.d("maguro", "AttachmentSynqService#onDownload uid:" + uid);
        Log.d("maguro",
            "AttachmentSynqService#onDownload downloadList.contains(uid):" + downloadList.contains(uid));
        if (!downloadList.contains(uid)) {
            download(account, folder, uid);
            downloadList.add(uid);
        }
        Log.d("maguro", "AttachmentSynqService#onDownload end");
    }

    private void download(final Account account, final String folder, final String uid)
                    throws MessagingException {
        Log.d("maguro", "AttachmentSynqService#download start");
        Log.d("maguro", "AttachmentSynqService#download uid:" + uid);
        Folder remoteFolder = null;
        LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folder);
            localFolder.open(OpenMode.READ_WRITE);

            Message message = localFolder.getMessage(uid);

            if (message.isSet(Flag.X_DOWNLOADED_FULL)) {
                Log.d("maguro", "AttachmentSynqService#download X_DOWNLOADED_FULL");
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.BODY);
                localFolder.fetch(new Message[] { message }, fp, null);
            } else {
                Log.d("maguro", "AttachmentSynqService#download NOT X_DOWNLOADED_FULL");
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

            Intent intent = new Intent();
            intent.putExtra("UID", uid);
            intent.setAction(ACTION);
            sendBroadcast(intent);
        } finally {
            Log.d("maguro", "AttachmentSynqService#download finally");
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
        Log.d("maguro", "AttachmentSynqService#download end");
    }

    private void closeFolder(Folder f) {
        if (f != null) {
            f.close();
        }
    }
}