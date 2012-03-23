/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.database.CursorIndexOutOfBoundsException;
import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.*;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.UnavailableStorageException;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class SlideMessage {
    private SlideMessage() {
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

        LocalStore.LocalFolder localFolder = null;
        try {
            localFolder = getLocalFolder(null, account, folder);
            LocalStore.LocalMessage message = (LocalStore.LocalMessage) localFolder.getMessage(uid);
            if (null == message) {
                return null;
            }
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.BODY);
            localFolder.fetch(new Message[]{message}, fp, null);
            return message;
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return null;
    }

    /**
     * @param account user account
     * @param folder  user folder name
     * @param uid     message uid
     * @return message(LocalStore.LocalMessage)
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static LocalStore.LocalMessage getNextLocalMessage(final Account account, final String folder, final String uid) {
        LocalStore.LocalFolder localFolder = null;
        try {
            localFolder = getLocalFolder(null, account, folder);
            LocalStore.LocalMessage message = (LocalStore.LocalMessage) localFolder.getNextMessage(uid, localFolder.getId());

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.BODY);
            localFolder.fetch(new Message[]{message}, fp, null);
            return message;
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return null;
    }

    /**
     * @param account user account
     * @param folder  user folder name
     * @param uid     message uid
     * @return message(LocalStore.LocalMessage)
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static LocalStore.LocalMessage getPreLocalMessage(final Account account, final String folder, final String uid) {
        LocalStore.LocalFolder localFolder = null;
        try {
            localFolder = getLocalFolder(null, account, folder);
            LocalStore.LocalMessage message = (LocalStore.LocalMessage) localFolder.getPreMessage(uid, localFolder.getId());

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.BODY);
            localFolder.fetch(new Message[]{message}, fp, null);
            return message;
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return null;
    }

    public static LocalStore.MessageInfo getMessageInfo(final Account account, final String folder, final String uid) {
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            return localStore.getMessage(uid, localFolder.getId());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#getMessageInfo error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return null;
    }

    public static LocalStore.MessageInfo getNextMessageInfo(final Account account, final String folder, final String uid) {
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            return localStore.getNextMessage(uid, localFolder.getId());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#getNextMessageInfo error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return null;
    }

    public static LocalStore.MessageInfo getPreMessageInfo(final Account account, final String folder, final String uid) {
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            return localStore.getPreMessage(uid, localFolder.getId());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#getPreMessageInfo error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return null;
    }

    /**
     * @param account user account
     * @param folder  user folder name
     * @return messageInfoList
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static ArrayList<LocalStore.MessageInfo> getMessageInfoList(final Account account, final String folder, final String uid, final long limitCount) {
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            if (null != uid && 0 != limitCount) {
                return localStore.getMessages(localFolder.getId(), uid, limitCount);
            } else if (null == uid && 0 != limitCount) {
                return localStore.getMessages(localFolder.getId(), limitCount);
            } else {
                return localStore.getMessages(localFolder.getId());
            }

        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#getMessMessageMdfafaageInfoList error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return null;
    }

    /**
     * 現在よりも新しいスライド対象メールが<br>
     * 存在する場合はtrue、存在しない場合はfalse.
     *
     * @param account account info
     * @param folder  receive mail folder name
     * @param uid     message uid
     * @return next Message?
     */
    public static boolean isNextMessage(final Account account, final String folder, final String uid) {
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            LocalStore.MessageInfo message = localStore.getNextMessage(uid, localFolder.getId());
            return isSlide(localStore, message.getId());
        } catch (UnavailableStorageException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#isPreMessage error:" + e);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#isNextMessage error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return false;
    }

    /**
     * 現在よりも古いスライド対象メールが<br>
     * 存在する場合はtrue、存在しない場合はfalse.
     *
     * @param account account info
     * @param folder  receive mail folder name
     * @param uid     message uid
     * @return next Message?
     */
    public static boolean isPreMessage(final Account account, final String folder, final String uid) {
        Log.d("refs#2611", "SlideMessage#isPreMessage");
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;

        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);

            LocalStore.MessageInfo message = localStore.getPreMessage(uid, localFolder.getId());
            Log.d("refs#2611", "SlideMessage#isPreMessage message.getId():" + message.getId());
            return isSlide(localStore, message.getId());
        } catch (UnavailableStorageException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#isPreMessage error:" + e);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#isPreMessage error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        return false;
    }

    private static boolean isSlide(LocalStore localStore, long id) throws UnavailableStorageException {
        ArrayList<LocalStore.Attachments> list = localStore.getAttachmentList(id);
        for (LocalStore.Attachments attachments : list) {
            if (SlideCheck.isSlide(attachments)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<LocalStore.Attachments> getAttachmentList(final Account account, final String folder, final String uid) {
        LocalStore.LocalMessage message = getLocalMessage(account, folder, uid);
        LocalStore localStore;
        try {
            localStore = account.getLocalStore();
            return localStore.getAttachmentList(message.getId());
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "CursorIndexOutOfBoundsException:" + e);
        } catch (UnavailableStorageException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "UnavailableStorageException:" + e);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        }
        return null;
    }

    public static MessageBean getMessage(final Account account, final String folder, final String uid) throws RakuRakuException {
        Log.d("ahokato", "SlideMessage#getMessage start uid:" + uid);
        LocalStore.LocalMessage localMessage = getLocalMessage(account, folder, uid);
        if (null == localMessage) {
            throw new RakuRakuException("SlideMessage#getMesasge localMessage is null...");
        }
        LocalStore.MessageInfo messageInfo = getMessageInfo(account, folder, uid);
        if (null == messageInfo) {
            throw new RakuRakuException("SlideMessage#getMesasge messageInfo is null...");
        }
        MessageBean messageBean = null;
        try {
            messageBean = setMessageBean(account, localMessage, messageInfo);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
        }
        ArrayList<LocalStore.Attachments> attachmentsList = getAttachmentList(account, folder, uid);
        ArrayList<AttachmentBean> list = new ArrayList<AttachmentBean>();
        for (LocalStore.Attachments attachments : attachmentsList) {
            list.add(setAttachmentBean(attachments));
        }
        messageBean.setAttachmentBeanList(list);
        return messageBean;
    }

    public static MessageBean getNextMessage(final Account account, final String folder, final String uid) throws RakuRakuException {
        LocalStore.LocalMessage localMessage = getNextLocalMessage(account, folder, uid);
        if (null == localMessage) {
            throw new RakuRakuException("SlideMessage#getNextMessage localMessage is null...");
        }
        LocalStore.MessageInfo messageInfo = getNextMessageInfo(account, folder, uid);
        if (null == messageInfo) {
            throw new RakuRakuException("SlideMessage#getNextMessage messageInfo is null...");
        }
        MessageBean messageBean = null;
        try {
            messageBean = setMessageBean(account, localMessage, messageInfo);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
        }

        ArrayList<LocalStore.Attachments> attachmentsList = getAttachmentList(account, folder, messageBean.getUid());
        ArrayList<AttachmentBean> list = new ArrayList<AttachmentBean>();
        for (LocalStore.Attachments attachments : attachmentsList) {
            list.add(setAttachmentBean(attachments));
        }
        messageBean.setAttachmentBeanList(list);
        return messageBean;
    }

    public static MessageBean getPreMessage(final Account account, final String folder, final String uid) throws RakuRakuException {
        LocalStore.LocalMessage localMessage = getPreLocalMessage(account, folder, uid);
        if (null == localMessage) {
            throw new RakuRakuException("SlideMessage#getPreMessage localMessage is null...");
        }
        LocalStore.MessageInfo messageInfo = getPreMessageInfo(account, folder, uid);
        if (null == messageInfo) {
            throw new RakuRakuException("SlideMessage#getPreMessage messageInfo is null...");
        }
        MessageBean messageBean = null;
        try {
            messageBean = setMessageBean(account, localMessage, messageInfo);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
        }
        ArrayList<LocalStore.Attachments> attachmentsList = getAttachmentList(account, folder, messageBean.getUid());
        ArrayList<AttachmentBean> list = new ArrayList<AttachmentBean>();
        for (LocalStore.Attachments attachments : attachmentsList) {
            list.add(setAttachmentBean(attachments));
        }
        messageBean.setAttachmentBeanList(list);
        return messageBean;
    }

    public static String getNextUid(final Account account, final String folder, final String uid) {
        LocalStore.LocalMessage localMessage = getNextLocalMessage(account, folder, uid);
        return localMessage.getUid();
    }

    public static String getPreUid(final Account account, final String folder, final String uid) {
        LocalStore.LocalMessage localMessage = getPreLocalMessage(account, folder, uid);
        return localMessage.getUid();
    }

    public static ArrayList<String> getMessageUidRemoveTarget(final Account account) {
        LocalStore localStore;
        try {
            localStore = account.getLocalStore();
            return localStore.getMessageUidRemoveTarget();
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#isPreMessage error:" + e);
        }
        return new ArrayList<String>();
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
        if (f != null) {
            f.close();
        }
    }

    /**
     * @param localMessage local message
     * @param messageInfo  message info
     * @return MessageBean
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private static MessageBean setMessageBean(Account account, LocalStore.LocalMessage localMessage, LocalStore.MessageInfo messageInfo) throws MessagingException {
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

        if (!messageBean.isFlagAnswered()) {
            messageBean.setFlagAnswered(setFlagAnsweredSupplement(account, messageBean));
            if (messageBean.isFlagAnswered()) {
                List<String> list = new ArrayList<String>();
                list.addAll(Arrays.asList(flagList));
                list.add(Flag.ANSWERED.toString());
                String[] resultArray = new String[list.size()];
                account.getLocalStore().setFlagAnswered(messageBean.getUid(), list.toArray(resultArray));
            }
        }
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
     * @param flag        message.flag (rakuphoto DB) 参考：Flag.class(enum)
     * @param messageBean MessageBean
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private static void setFlag(String[] flag, MessageBean messageBean) throws MessagingException {
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

    private static boolean setFlagAnsweredSupplement(Account account, MessageBean messageBean) throws MessagingException {
        LocalStore localStore = account.getLocalStore();
        ArrayList<String> list = localStore.getHeadersReferences();
        return list.contains(messageBean.getMessageId());
    }

    public static ArrayList<String> getUidList(Account account, String folderName) throws MessagingException {
        Log.d("ahokato", "MessageSync#getUidList start");
        LocalStore.LocalFolder localFolder = null;
        LocalStore localStore = null;
        try {
            localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            return localFolder.getUidList();
        } finally {
            localStore = null;
            closeFolder(localFolder);
            localFolder = null;
        }
    }

    public static ArrayList<String> getUidList(Account account, String folderName, int length) throws MessagingException {
        Log.d("ahokato", "MessageSync#getUidList start");
        LocalStore.LocalFolder localFolder = null;
        LocalStore localStore = null;
        try {
            localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            return localFolder.getUidList(length);
        } finally {
            localStore = null;
            closeFolder(localFolder);
            localFolder = null;
        }
    }

    public static ArrayList<String> getUidList(Account account, String folderName, String uid, int length) throws MessagingException {
        Log.d("ahokato", "MessageSync#getUidList start");
        LocalStore.LocalFolder localFolder = null;
        LocalStore localStore = null;
        try {
            localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            return localFolder.getUidList(uid, length);
        } finally {
            localStore = null;
            closeFolder(localFolder);
            localFolder = null;
        }
    }

    public static String getHighestLocalUid(Account account, String folderName) throws MessagingException {
        Log.d("ahokato", "SlideMessage#getHighestLocalUid start");
        LocalStore.LocalFolder localFolder = null;
        LocalStore localStore = null;
        try {
            localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            ArrayList<String> list = localFolder.getUidList();
            if (!list.isEmpty()) {
                return list.get(list.size() - 1);
            } else {
                return null;
            }
        } finally {
            localStore = null;
            closeFolder(localFolder);
            localFolder = null;
        }
    }

    public static ArrayList<MessageBean> getMessageBeanList(Account account, String folderName, String startUid, int length) throws RakuRakuException, MessagingException {
        Log.d("ahokato", "SlideMessage#getMessageBeanList start");

        ArrayList<String> uidList = null;
        if (0 == length) {
            Log.d("ahokato", "SlideMessage#getMessageBeanList getUidList全件");
            uidList = getUidList(account, folderName);
        } else if (!RakuPhotoStringUtils.isNotBlank(startUid)) {
            Log.d("ahokato", "SlideMessage#getMessageBeanList getUidList指定件数");
            uidList = getUidList(account, folderName, length);
        } else {
            Log.d("ahokato", "SlideMessage#getMessageBeanList getUidList指定位置からの指定件数");
            uidList = getUidList(account, folderName, startUid, length);
        }
        Log.d("ahokato", "SlideMessage#getMessageBeanList uidList:" + uidList.size());

        ArrayList<MessageBean> result = new ArrayList<MessageBean>();
        for (String uid : uidList) {
            result.add(getMessage(account, folderName, uid));
        }
        uidList = null;
        Log.d("ahokato", "SlideMessage#getMessageBeanList result:" + result.size());
        return result;
    }
}
