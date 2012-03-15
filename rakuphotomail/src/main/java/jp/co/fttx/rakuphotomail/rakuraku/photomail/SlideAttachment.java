/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.*;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.provider.AttachmentProvider;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class SlideAttachment {
    private SlideAttachment() {
    }

    private static Bitmap photo;
    private static Bitmap thumbnail;

    public static Bitmap getBitmap(Context context, Display display, Account account, AttachmentBean attachmentBean) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Uri uri = AttachmentProvider.getAttachmentUri(account, attachmentBean.getId());
        if (null == uri) {
            return null;
        }
        options.inJustDecodeBounds = true;
        photo = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        int displayW = display.getWidth();
        int displayH = display.getHeight();
        int scaleRatio = account.getScaleRatio();
        int scaleW = options.outWidth / displayW + scaleRatio;
        int scaleH = options.outHeight / displayH + scaleRatio;
        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.max(scaleW, scaleH);
        photo = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        return photo;
    }

    public static Bitmap getThumbnailBitmap(Context context, Account account, AttachmentBean attachmentBean) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Uri uri = AttachmentProvider.getAttachmentUri(account, attachmentBean.getId());
            options.inJustDecodeBounds = true;
            context.getContentResolver().openInputStream(uri);
            thumbnail = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            int displayW = 150;
            int displayH = 100;
            int scaleW = options.outWidth / displayW + 1;
            int scaleH = options.outHeight / displayH + 1;
            options.inJustDecodeBounds = false;
            options.inSampleSize = Math.max(scaleW, scaleH);
            thumbnail = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null,
                    options);
            return thumbnail;
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage());
        }
        return null;
    }

    public static ArrayList<AttachmentBean> getSlideTargetList(ArrayList<AttachmentBean> origin) {
        ArrayList<AttachmentBean> dest = new ArrayList<AttachmentBean>();
        for (AttachmentBean bean : origin) {
            if (SlideCheck.isSlide(bean)) {
                dest.add(bean);
            }
        }
        return dest;
    }

    /**
     * <p>添付ファイルのキャッシュクリア.</p>
     * <p>実現機能</p>
     * <ul>
     * <li>添付ファイルのローカル物理削除（再同期、再DL可）</li>
     * <li>messages.flags X_DOWNLOADED_FULLの削除</li>
     * <li>attachments.content_uriのクリア</li>
     * </ul>
     *
     * @param account    user account info
     * @param folderName user mail folder name
     * @param uid        message uid
     * @throws MessagingException me
     * @throws RakuRakuException  rre
     */
    public static void clearCacheForAttachmentFile(Account account, String folderName, String uid) throws MessagingException, RakuRakuException {
        Log.d("ahokato", "SlideAttachment#clearCacheForAttachmentFile uid:" + uid);
        LocalStore localStore = account.getLocalStore();
        LocalStore.LocalFolder localFolder = localStore.getFolder(folderName);
        long[] attachmentIdList = localFolder.deleteAttachmentFile(uid);
        for (long attachmentId : attachmentIdList) {
            Log.d("ahokato", "SlideAttachment#clearCacheForAttachmentFile attachmentId:" + attachmentId);
            if (localFolder.clearContentUri(attachmentId)) {
                MessageBean messageBean = SlideMessage.getMessage(account, folderName, uid);
                String[] arr = RakuPhotoStringUtils.splitFlags(messageBean.getFlags());
                if (0 < arr.length) {
                    ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(arr));
                    int index = arrayList.indexOf("X_DOWNLOADED_FULL");
                    Log.d("ahokato", "SlideAttachment#clearCacheForAttachmentFile index:" + index);
                    if (0 <= index) {
                        arrayList.remove(index);
                        localStore.setFlagAnswered(uid, arrayList.toArray(new String[arrayList.size()]));
                    }
                }
            }
        }
    }

    public static void downloadAttachment(final Account account, final String folder, final String uid) {
        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folder);
            localFolder.open(Folder.OpenMode.READ_WRITE);

            Message message = localFolder.getMessage(uid);
            if (null == message || !message.isSet(Flag.X_DOWNLOADED_FULL)) {
                Store remoteStore = account.getRemoteStore();
                remoteFolder = remoteStore.getFolder(folder);
                remoteFolder.open(Folder.OpenMode.READ_WRITE);

                Message remoteMessage = remoteFolder.getMessage(uid);
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.BODY);
                remoteFolder.fetch(new Message[]{remoteMessage}, fp, null);

                localFolder.appendMessages(new Message[]{remoteMessage});
                fp.add(FetchProfile.Item.ENVELOPE);
                message = localFolder.getMessage(uid);
                localFolder.fetch(new Message[]{message}, fp, null);

                message.setFlag(Flag.X_DOWNLOADED_FULL, true);
            }
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:" + e.getMessage() + " UID:" + uid);
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
    }

    /**
     * 添付ファイルをダウンロードする際に、メッセージ毎同期してしまうので注意.
     *
     * @param account       user account
     * @param folder        user IMAP folder
     * @param remoteMessage user IMAP Server Message
     */
    public static void downloadAttachment(final Account account, final String folder, final Message remoteMessage) {
        final String uid = remoteMessage.getUid();
        downloadAttachment(account, folder, uid);
    }

    private static void closeFolder(Folder f) {
        if (f != null) {
            f.close();
        }
    }
}
