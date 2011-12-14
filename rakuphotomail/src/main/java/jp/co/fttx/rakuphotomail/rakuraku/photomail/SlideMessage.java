/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.database.CursorIndexOutOfBoundsException;
import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.FetchProfile;
import jp.co.fttx.rakuphotomail.mail.Folder;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.UnavailableStorageException;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

import java.util.ArrayList;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class SlideMessage {
    private SlideMessage() {
        Log.d("maguro", "SlideMessage Construct");
    }

    /**
     * @param account user account
     * @param folder  user folder name
     * @param uid     message uid
     * @return message(LocalStore.LocalMessage)
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static LocalStore.LocalMessage getLocalMessage(final Account account, final String folder, final String uid) {
        Log.d("maguro", "SlideMessage#getLocalMessage start");
        LocalStore.LocalFolder localFolder = null;
        try {
            localFolder = getLocalFolder(null, account, folder);
            LocalStore.LocalMessage message = (LocalStore.LocalMessage) localFolder.getMessage(uid);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.BODY);
            localFolder.fetch(new Message[]{message}, fp, null);
            Log.d("maguro", "SlideMessage#getLocalMessage normal end");
            return message;
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        } finally {
            closeFolder(localFolder);
        }
        Log.d("maguro", "SlideMessage#getLocalMessage abnormal end");
        return null;
    }

    public static LocalStore.MessageInfo getMessageInfo(final Account account, final String folder, final String uid) {
        Log.d("maguro", "SlideMessage#getMessageInfo start");
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            Log.d("maguro", "SlideMessage#getMessageInfoList nomal end");
            return localStore.getMessage(uid);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#getMessageInfoList error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        Log.d("maguro", "SlideMessage#getMessageInfo abnormal end");
        return null;
    }

    /**
     * @param account user account
     * @param folder  user folder name
     * @return messageInfoList
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static ArrayList<LocalStore.MessageInfo> getMessageInfoList(final Account account, final String folder) {
        Log.d("maguro", "SlideMessage#getMessageInfoList start");
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            Log.d("maguro", "SlideMessage#getMessageInfoList nomal end");
            return localStore.getMessages(localFolder.getId());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#getMessageInfoList error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        Log.d("maguro", "SlideMessage#getMessageInfoList abnomal end");
        return null;
    }

    public static ArrayList<LocalStore.Attachments> getAttachmentList(final Account account, final String folder, final String uid) {
        Log.d("maguro", "SlideMessage#getAttachmentList start");
        LocalStore.LocalMessage message = getLocalMessage(account, folder, uid);
        LocalStore localStore;
        try {
            localStore = account.getLocalStore();
            Log.d("maguro", "SlideMessage#getAttachmentList normal end");
            return localStore.getAttachmentList(message.getId());
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "CursorIndexOutOfBoundsException:" + e);
        } catch (UnavailableStorageException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "UnavailableStorageException:" + e);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        }
        Log.d("maguro", "SlideMessage#getAttachmentList abnormal end");
        return null;
    }

    public static MessageBean getMessage(final Account account, final String folder, final String uid) throws RakuRakuException {
        Log.d("maguro", "SlideMessage#getMesasge start");
        LocalStore.LocalMessage localMessage = getLocalMessage(account, folder, uid);
        if (null == localMessage) {
            throw new RakuRakuException("SlideMessage#getMesasge localMessage is null...");
        }
        LocalStore.MessageInfo messageInfo = getMessageInfo(account, folder, uid);
        if (null == messageInfo) {
            throw new RakuRakuException("SlideMessage#getMesasge messageInfo is null...");
        }
        MessageBean messageBean = setMessageBean(localMessage, messageInfo);
        ArrayList<LocalStore.Attachments> attachmentsList = getAttachmentList(account, folder, uid);
        ArrayList<AttachmentBean> list = new ArrayList<AttachmentBean>();
        for (LocalStore.Attachments attachments : attachmentsList) {
            list.add(setAttachmentBean(attachments));
        }
        messageBean.setAttachmentBeanList(list);
        Log.d("maguro", "SlideMessage#getMesasge end");
        return messageBean;
    }

    private static LocalStore.LocalFolder getLocalFolder(LocalStore localStore, final Account account, final String folder) throws MessagingException {
        if (null == localStore) {
            localStore = account.getLocalStore();
        }
        LocalStore.LocalFolder localFolder = localStore.getFolder(folder);
        localFolder.open(Folder.OpenMode.READ_WRITE);
        return localFolder;
    }

    private static void closeFolder(Folder f) {
        Log.d("maguro", "SlideMessage#closeFolder start");
        if (f != null) {
            f.close();
        }
        Log.d("maguro", "SlideMessage#closeFolder end");
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     * @param localMessage
     * @param messageInfo
     * @return MessageBean
     */
    private static MessageBean setMessageBean(LocalStore.LocalMessage localMessage, LocalStore.MessageInfo messageInfo) {
        Log.d("maguro", "SlideMessage#setMessage start");
        MessageBean messageBean = new MessageBean();
        messageBean.setId(localMessage.getId());
        messageBean.setDeleted(messageInfo.getDeleted());
        messageBean.setFolderId(messageInfo.getFolderId());
        messageBean.setUid(localMessage.getUid());
        messageBean.setSubject(localMessage.getSubject());
        messageBean.setDate(messageInfo.getDate());
        String flags = messageInfo.getFlags();
        messageBean.setFlags(flags);
        String[] flagList = RakuPhotoStringUtils.splitFlags(flags);
        if (null != flagList && flagList.length != 0) {
            setFlag(flagList, messageBean);
        }
        messageBean.setSenderList(messageInfo.getSenderList());
        String[] mailFromArr = messageInfo.getSenderList().split(";");
        if (mailFromArr == null || mailFromArr.length == 0) {
        } else if (mailFromArr.length == 1) {
            messageBean.setSenderAddress(mailFromArr[0]);
        } else {
            messageBean.setSenderAddress(mailFromArr[0]);
            messageBean.setSenderName(mailFromArr[1]);
        }
        messageBean.setToList(messageInfo.getToList());
        messageBean.setCcList(messageInfo.getCcList());
        messageBean.setBccList(messageInfo.getBccList());
        messageBean.setReplyToList(messageInfo.getReplyToList());
        messageBean.setHtmlContent(messageInfo.getHtmlContent());
        messageBean.setTextContent(messageInfo.getTextContent());
        messageBean.setAttachmentCount(localMessage.getAttachmentCount());
        messageBean.setInternalDate(messageInfo.getInternalDate());
        messageBean.setMessageId(messageInfo.getMessageId());
        messageBean.setPreview(messageInfo.getPreview());
        messageBean.setMimeType(messageInfo.getMimeType());
        messageBean.setMessage(localMessage);
        Log.d("maguro", "SlideMessage#setMessage end");
        return messageBean;
    }

    /**
     * setFlag.
     * <ul>
     * <li>X_GOT_ALL_HEADERS</li>
     * <li>X_DOWNLOADED_FULL</li>
     * <li>SEEN</li>
     * <li>ANSWERED</li>
     * <li>X_DOWNLOADED_PARTIAL</li>
     * <li>X_REMOTE_COPY_STARTED</li>
     * </ul>
     *
     * @param flag        message.flag (rakuphoto DB)
     * @param messageBean MessageBean
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private static void setFlag(String[] flag, MessageBean messageBean) {
        Log.d("maguro", "SlideMessage#setFlag start");
        StringBuilder builder = new StringBuilder();
        for (String f : flag) {
            if ("X_GOT_ALL_HEADERS".equals(f)) {
                messageBean.setFlagXGotAllHeaders(true);
            } else if ("SEEN".equals(f)) {
                messageBean.setFlagSeen(true);
            } else if ("ANSWERED".equals(f)) {
                messageBean.setFlagAnswered(true);
            } else if ("X_DOWNLOADED_FULL".equals(f)) {
                messageBean.setFlagXDownLoadedFull(true);
            } else if ("X_DOWNLOADED_PARTIAL".equals(f)) {
                messageBean.setFlagXDownLoadedPartial(true);
            } else if ("X_REMOTE_COPY_STARTED".equals(f)) {
                messageBean.setFlagXRemoteCopyStarted(true);
            } else {
                builder.append(f);
                builder.append(",");
            }
            int len = builder.length();
            if (0 != len) {
                messageBean.setFlagOther(builder.delete(len - 1, len).toString());
            }
        }
        Log.d("maguro", "SlideMessage#setFlag end");
    }

    private static AttachmentBean setAttachmentBean(LocalStore.Attachments attachments) {
        AttachmentBean attachmentBean = new AttachmentBean();
        attachmentBean.setId(attachments.getId());
        attachmentBean.setMessageId(attachments.getMessageId());
        attachmentBean.setStoreData(attachments.getStoreData());
        attachmentBean.setContentUrl(attachments.getContentUri());
        attachmentBean.setSize(attachments.getSize());
        attachmentBean.setName(attachments.getName());
        attachmentBean.setMimeType(attachments.getMimeType());
        attachmentBean.setContentId(attachments.getContentId());
        attachmentBean.setContentDisposition(attachments.getContentDisposition());
        return attachmentBean;
    }
}
