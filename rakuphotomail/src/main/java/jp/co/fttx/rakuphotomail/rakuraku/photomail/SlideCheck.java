/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.util.Log;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;

import java.util.ArrayList;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class SlideCheck {
    private SlideCheck() {
        Log.d("maguro", "SlideCheck Construct");
    }

    /**
     * @param attachment Attachments
     * @return slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static boolean isSlide(LocalStore.Attachments attachment) {
        String mimeType = attachment.getMimeType();
        String fileName = attachment.getName();
        return isSlide(mimeType, fileName);
    }

    /**
     * @param attachment AttachmentBean
     * @return slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static boolean isSlide(AttachmentBean attachment) {
        String mimeType = attachment.getMimeType();
        String fileName = attachment.getName();
        return isSlide(mimeType, fileName);
    }

    /**
     * @param messageBean
     * @return
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static boolean isDownloadedAttachment(MessageBean messageBean) {
        ArrayList<AttachmentBean> attachmentBeanList = messageBean.getAttachmentBeanList();
        for (AttachmentBean attachmentBean : attachmentBeanList) {
            if (null == attachmentBean.getContentUrl()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param mimeType mime type
     * @param fileName extension check
     * @return slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private static boolean isSlide(String mimeType, String fileName) {
        return isMimeType(mimeType) || isExtension(fileName);
    }

    /**
     * @param mimeType mime type
     * @return slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private static boolean isMimeType(String mimeType) {
        return "image/jpeg".equals(mimeType) || "image/png".equals(mimeType);
    }

    /**
     * @param fileName extension check
     * @return slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private static boolean isExtension(String fileName) {
        return null != fileName && (fileName.endsWith(".png") || fileName.endsWith(".PNG") || fileName.endsWith(".JPG")
                || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"));
    }

}
