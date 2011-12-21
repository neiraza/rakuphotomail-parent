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
import android.view.WindowManager;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.provider.AttachmentProvider;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class SlideAttachment {
    private SlideAttachment() {
        Log.d("maguro", "SlideAttachment Construct");
    }

    public static Bitmap getBitmap(Context context, Display display, Account account, AttachmentBean attachmentBean) {
        Log.d("maguro", "SlideAttachment#getBitmap");
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
            Log.d("maguro", "SlideAttachment#getBitmap いけそう？");
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null,
                    options);
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Exception:" + e);
        }
        return null;
    }

    public static Bitmap getThumbnailBitmap(Context context, Display display, Account account, AttachmentBean attachmentBean) {
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

}
