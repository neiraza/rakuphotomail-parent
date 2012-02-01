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
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.mail.*;
import jp.co.fttx.rakuphotomail.mail.Folder.OpenMode;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class AttachmentSyncService extends Service {

    /**
     *
     */
    public static final String ACTION_SLIDE_SHOW = "jp.co.fttx.rakuphotomail.service.AttachmentSyncService.action.SLIDE_SHOW";
    /**
     *
     */
    public static final String ACTION_SLIDE_SHOW_STOP = "jp.co.fttx.rakuphotomail.service.AttachmentSyncService.action.SLIDE_SHOW_STOP";
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
     *
     */
    private final IBinder mBinder = new AttachmentSyncBinder();
    /**
     *
     */
    private List<String> downloadList;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadList = new ArrayList<String>();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("refs1961", "AttachmentSyncService#onStart");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("refs1961", "AttachmentSyncService#onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("refs1961", "AttachmentSyncService#onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("refs1961", "AttachmentSyncService#onDestroy");
        super.onDestroy();
    }

    public class AttachmentSyncBinder extends Binder {
        public AttachmentSyncService getService() {
            Log.d("refs1961", "AttachmentSyncBinder#getService");
            return AttachmentSyncService.this;
        }
    }

    /**
     * <p>
     * ダウンロード処理.
     * </p>
     * downloadListに含まれていないuidのメールのみ、ダウンロードの対象とする。<br>
     * これはスライドショーの中で、ダウンロードリクエストが非同期に送信される前提で、<br>
     * 同じリクエストが複数回無駄に送信される事を避けるため。<br>
     * d
     *
     * @param folder フォルダー名
     * @param uid    メール識別番号
     * @throws MessagingException oyakusoku
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public void onDownload(final Account account, final String folder, final String uid, final String action)
            throws MessagingException {
        Log.d("asakusa", "AttachmentSyncService#onDownload uid:" + uid);
        Log.d("asakusa", "AttachmentSyncService#onDownload !downloadList.contains(uid):" + !downloadList.contains(uid));

        if (!downloadList.contains(uid)) {
            download(account, folder, uid);
            downloadList.add(uid);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
            intent.putExtra(EXTRA_FOLDER, folder);
            intent.putExtra(EXTRA_UID, uid);
            if (ACTION_SLIDE_SHOW.equals(action)) {
                intent.setAction(ACTION_SLIDE_SHOW);
            } else if (ACTION_SLIDE_SHOW_STOP.equals(action)) {
                intent.setAction(ACTION_SLIDE_SHOW_STOP);
            } else {
                intent.setAction("");
            }
            sendBroadcast(intent);
        }
    }

    /**
     * @param account アカウント情報
     * @param folder  フォルダー名
     * @param uid     メール識別番号
     * @throws MessagingException oyakusoku
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void download(final Account account, final String folder, final String uid)
            throws MessagingException {
        Log.d("asakusa", "AttachmentSyncService#download uid:" + uid);
        Folder remoteFolder = null;
        LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folder);
            localFolder.open(OpenMode.READ_WRITE);

            Message message = localFolder.getMessage(uid);
            if (message.isSet(Flag.X_DOWNLOADED_FULL)) {
                Log.d("asakusa", "AttachmentSyncService#download 地獄");
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.BODY);
                localFolder.fetch(new Message[]{message}, fp, null);
            } else {
                Log.d("asakusa", "AttachmentSyncService#download 天国");
                Store remoteStore = account.getRemoteStore();
                remoteFolder = remoteStore.getFolder(folder);
                remoteFolder.open(OpenMode.READ_WRITE);

                Message remoteMessage = remoteFolder.getMessage(uid);
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.BODY);
                remoteFolder.fetch(new Message[]{remoteMessage}, fp, null);

                localFolder.appendMessages(new Message[]{remoteMessage});
                fp.add(FetchProfile.Item.ENVELOPE);
                message = localFolder.getMessage(uid);
                localFolder.fetch(new Message[]{message}, fp, null);

                message.setFlag(Flag.X_DOWNLOADED_FULL, true);
                Log.d("refs2608@", "AttachmentSyncService#download end");
            }
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
    }

    private void closeFolder(Folder f) {
        if (f != null) {
            f.close();
        }
    }

//    public void clearDownloadedUid(String uid) {
//        int index = downloadList.indexOf(uid);
//        if (0 < index) {
//            downloadList.remove(index);
//        }
//    }
//
//    public void clearAllDownloadedUid() {
//        downloadList = new ArrayList<String>();
//        GallerySlideShow.hoge();
//    }

}
