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
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.provider.AttachmentProvider;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class SlideAttachment {
    private SlideAttachment() {
    }

    public static Bitmap getBitmap(Context context, Display display, Account account, AttachmentBean attachmentBean) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Uri uri = AttachmentProvider.getAttachmentUri(account, attachmentBean.getId());
            options.inJustDecodeBounds = true;
            context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            int displayW = display.getWidth();
            int displayH = display.getHeight();
            int scaleW = options.outWidth / displayW + 1;
            int scaleH = options.outHeight / displayH + 1;
            options.inJustDecodeBounds = false;
            options.inSampleSize = Math.max(scaleW, scaleH);
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null,
                    options);
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Exception:" + e);
        }
        return null;
    }

    public static Bitmap getThumbnailBitmap(Context context, Account account, AttachmentBean attachmentBean) {
        Log.d("maguro", "SlideAttachment#getThumbnailBitmap");
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Uri uri = AttachmentProvider.getAttachmentUri(account, attachmentBean.getId());
            options.inJustDecodeBounds = true;
            context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            int displayW = 150;
            int displayH = 100;
            int scaleW = options.outWidth / displayW + 1;
            int scaleH = options.outHeight / displayH + 1;
            options.inJustDecodeBounds = false;
            options.inSampleSize = Math.max(scaleW, scaleH);
            Log.d("maguro", "SlideAttachment#getThumbnailBitmap Thumbnailいけそう？");
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null,
                    options);
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Exception:" + e);
        }
        return null;
    }

    public static ArrayList<AttachmentBean> getSlideTargetList(ArrayList<AttachmentBean> origin) {
        ArrayList<AttachmentBean> dest = new ArrayList<AttachmentBean>();
        for (AttachmentBean bean : origin) {
            Log.d("ucom", "bean.getId():" + bean.getId());
            Log.d("ucom", "bean.getName():" + bean.getName());
            if (SlideCheck.isSlide(bean)) {
                Log.d("ucom", bean.getId() + " is OK");
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
     */
    public static void clearCacheForAttachmentFile(Account account, String folderName, String uid) throws MessagingException {
        LocalStore.LocalFolder localFolder = null;
        LocalStore localStore = account.getLocalStore();
        localFolder = localStore.getFolder(folderName);
        long[] attachmentIdList = localFolder.deleteAttachmentFile(uid);
        for (long attachmentId : attachmentIdList) {
            Log.d("asakusa", "clearCacheForAttachmentFile attachmentId:" + attachmentId);
            if (localFolder.clearContentUri(attachmentId)) {
                MessageBean messageBean = new MessageBean();
                try {
                    messageBean = SlideMessage.getMessage(account, account.getInboxFolderName(), uid);
                } catch (RakuRakuException e) {
                    e.printStackTrace();
                }
                String[] arr = RakuPhotoStringUtils.splitFlags(messageBean.getFlags());
                if (0 < arr.length) {
                    ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(arr));
                    int index = arrayList.indexOf("X_DOWNLOADED_FULL");
                    Log.d("asakusa", "clearCacheForAttachmentFile index:" + index);
                    if (0 < index) {
                        arrayList.remove(index);
                        localStore.setFlagAnswered(uid, arrayList.toArray(new String[arrayList.size()]));
                    }
                }
            }
        }
    }
}
