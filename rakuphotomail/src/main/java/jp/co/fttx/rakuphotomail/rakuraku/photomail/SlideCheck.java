/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.util.Log;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Part;
import jp.co.fttx.rakuphotomail.mail.internet.MimeUtility;
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
    }

    /**
     * @param attachment attachment
     * @return slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static boolean isSlide(Part attachment) throws MessagingException {
        String mimeType = attachment.getMimeType();
        String fileName = MimeUtility.unfoldAndDecode(MimeUtility.getHeaderParameter(
                attachment.getContentType(), "name"));
        return isSlide(mimeType, fileName);
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
     * @param bean AttachmentBean
     * @return slide OK/NG
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static boolean isSlide(AttachmentBean bean) {
        String mimeType = bean.getMimeType();
        String fileName = bean.getName();
        return isSlide(mimeType, fileName);
    }

    /**
     * @param messageBean message
     * @return download attachment ?
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static boolean isDownloadedAttachment(MessageBean messageBean) {
        Log.d("ahokato", "SlideCheck#isDownloadedAttachment(MessageBean messageBean) start");
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

    public static boolean isSlide(Message message) throws MessagingException {
        ArrayList<Part> Unnecessary = new ArrayList<Part>();
        ArrayList<Part> attachments = new ArrayList<Part>();
        MimeUtility.collectParts(message, Unnecessary, attachments);
        for (Part attachment : attachments) {
            if (SlideCheck.isSlide(attachment)) {
                return true;
            }
        }
        Unnecessary = null;
        attachments = null;
        return false;
    }
}
