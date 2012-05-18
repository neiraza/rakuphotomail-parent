/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.bean;

import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalMessage;

import java.util.ArrayList;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class MessageBean {
    /**
     * messages.id(DB)
     */
    private long id;
    /**
     * messages.deleted(DB)
     */
    private int deleted;
    /**
     * messages.folderId(DB)
     */
    private long folderId;
    /**
     * messages.uid(DB)
     */
    private String uid;
    /**
     * messages.subject(DB)
     */
    private String subject;
    /**
     * messages.date(DB)
     */
    private long date;
    /**
     * messages.flags(DB)
     */
    private String flags;
    /**
     * flags X_GOT_ALL_HEADERS
     */
    private boolean flagXGotAllHeaders;
    /**
     * flags SEEN
     */
    private boolean flagSeen;
    /**
     * flags ANSWERED
     */
    private boolean flagAnswered;
    /**
     * flags X_DOWNLOADED_FULL
     */
    private boolean flagXDownLoadedFull;
    /**
     * flags X_DOWNLOADED_PARTIAL
     */
    private boolean flagXDownLoadedPartial;
    /**
     * flags X_REMOTE_COPY_STARTED
     */
    private boolean flagXRemoteCopyStarted;
    /**
     * flags other...
     */
    private String flagOther;
    /**
     * messages.senderList(DB)
     */
    private String senderList;
    /**
     * senderList address
     */
    private String senderAddress;
    /**
     * senderList name
     */
    private String senderName;
    /**
     * messages.toList(DB)
     */
    private String toList;
    /**
     * messages.ccList(DB)
     */
    private String ccList;
    /**
     * messages.bccList(DB)
     */
    private String bccList;
    /**
     * messages.replyToList(DB)
     */
    private String replyToList;
    /**
     * messages.htmlContent(DB)
     */
    private String htmlContent;
    /**
     * messages.textContent(DB)
     */
    private String textContent;
    /**
     * messages.attachmentCount(DB)
     */
    private long attachmentCount;
    /**
     * messages.internalDate(DB)
     */
    private long internalDate;
    /**
     * messages.messageId(DB)
     */
    private String messageId;
    /**
     * messages.preview(DB)
     */
    private String preview;
    /**
     * messages.mimeType(DB)
     */
    private String mimeType;
    /**
     * attachments
     */
    private ArrayList<AttachmentBean> attachmentBeanList;
    /**
     * LocalMessage
     */
    private LocalMessage message;

    public MessageBean() {
        attachmentBeanList = new ArrayList<AttachmentBean>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getSenderList() {
        return senderList;
    }

    public void setSenderList(String senderList) {
        this.senderList = senderList;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getToList() {
        return toList;
    }

    public void setToList(String toList) {
        this.toList = toList;
    }

    public String getCcList() {
        return ccList;
    }

    public void setCcList(String ccList) {
        this.ccList = ccList;
    }

    public String getBccList() {
        return bccList;
    }

    public void setBccList(String bccList) {
        this.bccList = bccList;
    }

    public String getReplyToList() {
        return replyToList;
    }

    public void setReplyToList(String replyToList) {
        this.replyToList = replyToList;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public long getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(long attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public ArrayList<AttachmentBean> getAttachmentBeanList() {
        return attachmentBeanList;
    }

    public void setAttachmentBeanList(ArrayList<AttachmentBean> attachmentBeanList) {
        this.attachmentBeanList = attachmentBeanList;
    }

    public LocalMessage getMessage() {
        return message;
    }

    public void setMessage(LocalMessage message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public boolean isFlagXGotAllHeaders() {
        return flagXGotAllHeaders;
    }

    public void setFlagXGotAllHeaders(boolean flagXGotAllHeaders) {
        this.flagXGotAllHeaders = flagXGotAllHeaders;
    }

    public boolean isFlagSeen() {
        return flagSeen;
    }

    public void setFlagSeen(boolean flagSeen) {
        this.flagSeen = flagSeen;
    }

    public boolean isFlagAnswered() {
        return flagAnswered;
    }

    public void setFlagAnswered(boolean flagAnswered) {
        this.flagAnswered = flagAnswered;
    }

    public boolean isFlagXDownLoadedFull() {
        return flagXDownLoadedFull;
    }

    public void setFlagXDownLoadedFull(boolean flagXDownLoadedFull) {
        this.flagXDownLoadedFull = flagXDownLoadedFull;
    }

    public boolean isFlagXDownLoadedPartial() {
        return flagXDownLoadedPartial;
    }

    public void setFlagXDownLoadedPartial(boolean flagXDownLoadedPartial) {
        this.flagXDownLoadedPartial = flagXDownLoadedPartial;
    }

    public boolean isFlagXRemoteCopyStarted() {
        return flagXRemoteCopyStarted;
    }

    public void setFlagXRemoteCopyStarted(boolean flagXRemoteCopyStarted) {
        this.flagXRemoteCopyStarted = flagXRemoteCopyStarted;
    }

    public String getFlagOther() {
        return flagOther;
    }

    public void setFlagOther(String flagOther) {
        this.flagOther = flagOther;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public long getInternalDate() {
        return internalDate;
    }

    public void setInternalDate(long internalDate) {
        this.internalDate = internalDate;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String toString() {
        return id + "," + uid + "," + subject + "," + mimeType;
    }
}
