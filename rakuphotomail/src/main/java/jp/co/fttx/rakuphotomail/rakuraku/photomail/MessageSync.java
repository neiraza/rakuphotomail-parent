/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.controller.MessagingController;
import jp.co.fttx.rakuphotomail.controller.MessagingListener;
import jp.co.fttx.rakuphotomail.mail.*;
import jp.co.fttx.rakuphotomail.mail.internet.MimeUtility;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class MessageSync {

    //    private static Set<MessagingListener> mListeners = new CopyOnWriteArraySet<MessagingListener>();
    private static MessagingListener mListener;
    private static MessagingController.MemorizingListener memorizingListener = new MessagingController.MemorizingListener();

    /**
     * @param account    User Account Info(Account Class)
     * @param folderName Folder Name(String)
     *                   <p>Ex.Folder Name</p>
     *                   <ul>
     *                   <li>account.getInboxFolderName()</li>
     *                   <li>account.getOutboxFolderName()</li>
     *                   <li>account.getSentFolderName()</li>
     *                   <li>account.getArchiveFolderName()</li>
     *                   <li>account.getAutoExpandFolderName()</li>
     *                   </ul>
     *                   件数が多い場合、こいつを使うともれなくOOMEします
     * @return String NewMail Uid
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static String syncMailboxForCheckNewMail(Account account, String folderName, int messageLimitCountFromRemote) {
        String newMailUid = null;

        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        FetchProfile fp = null;
        Message[] message1 = null;
        Message[] message2 = null;
        Message[] message3 = null;
        Store remoteStore = null;
        Message[] remoteMessageArray = null;
        HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();

        try {
            //ローカル側の更新前データ保持
            localFolder = account.getLocalStore().getFolder(folderName);
            localFolder.open(Folder.OpenMode.READ_WRITE);
            localFolder.updateLastUid(); //こいつで一番最後のUIDを保持（localFolder.getLastUid();で取得）

            Message[] localMessages = localFolder.getMessages(null);
            HashMap<String, Message> localUidMap = new HashMap<String, Message>();
            for (Message message : localMessages) {
                localUidMap.put(message.getUid(), message);
            }

            remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();

            if (remoteMessageCount > 0) {
                int remoteStart = getRemoteStart(remoteMessageCount, messageLimitCountFromRemote);
                int remoteEnd = remoteMessageCount;
                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, null, null);
                for (Message thisMessage : remoteMessageArray) {
                    Message localMessage = localUidMap.get(thisMessage.getUid()); // このUIDは更新前もあるかなー？
                    remoteUidMap.put(thisMessage.getUid(), thisMessage); // 新規に増えたやつかな
                    fp = null;
                    message1 = null;
                    message2 = null;
                    message3 = null;
                    if (localMessage == null) {
                        fp = new FetchProfile();
                        fp.add(FetchProfile.Item.BODY);
                        message1 = new Message[]{thisMessage};
                        remoteFolder.fetch(message1, fp, null);
                        message2 = new Message[]{thisMessage};
                        localFolder.appendMessages(message2);
                        fp.add(FetchProfile.Item.ENVELOPE);
                        Message lMessage = localFolder.getMessage(thisMessage.getUid());
                        message3 = new Message[]{lMessage};
                        localFolder.fetch(message3, fp, null);
                        lMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);
                        newMailUid = thisMessage.getUid();
                    }
                }
            } else if (remoteMessageCount < 0) {
                return null;
            }
            ArrayList<Message> destroyMessages = new ArrayList<Message>();
            for (Message localMessage : localMessages) {
                if (remoteUidMap.get(localMessage.getUid()) == null) {
                    destroyMessages.add(localMessage);
                }
            }
            localFolder.destroyMessages(destroyMessages.toArray(new Message[0]));
            localMessages = null;

            setLocalFlaggedCountToRemote(localFolder, remoteFolder);

            localFolder.setLastChecked(System.currentTimeMillis());
            localFolder.setStatus(null);

        } catch (MessagingException me) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:MessagingException:" + me.getMessage());
            return null;
        } finally {
            fp = null;
            message1 = null;
            message2 = null;
            message3 = null;
            remoteMessageArray = null;
            remoteUidMap = null;
            closeFolder(remoteFolder);
            closeFolder(localFolder);
            localFolder = null;
            remoteFolder = null;
            remoteStore = null;
        }
        return newMailUid;
    }

    public static boolean isSlideRemoteMail(final Account account, final String folder, final Message remoteMessage) throws MessagingException {

        Folder remoteFolder = null;
        FetchProfile fp = null;
        ArrayList<Part> Unnecessary = null;
        ArrayList<Part> attachments = null;
        Message[] messages = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folder);

            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            messages = new Message[]{remoteMessage};
            remoteFolder.fetch(messages, fp, null);

            Unnecessary = new ArrayList<Part>();
            attachments = new ArrayList<Part>();
            MimeUtility.collectParts(remoteMessage, Unnecessary, attachments);

            for (Part attachment : attachments) {
                if (SlideCheck.isSlide(attachment)) {
                    return true;
                }
            }
            return false;
        } finally {
            fp = null;
            Unnecessary = null;
            attachments = null;
            messages = null;
            closeFolder(remoteFolder);
        }
    }

    public static String getRemoteUid(final Account account, final String folderName, int messageId) throws MessagingException, RakuRakuException {

        Folder remoteFolder = null;
        Store remoteStore;
        try {
            remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            Message[] remoteMessageArray = remoteFolder.getMessages(messageId, messageId, null, null);

            if (0 != remoteMessageArray.length) {
                Message thisMessage = remoteMessageArray[0];
                if (null != thisMessage) {
                    return thisMessage.getUid();
                }
            }

            remoteMessageArray = null;
            return null;
        } finally {
            closeFolder(remoteFolder);
            remoteFolder = null;
            remoteStore = null;
        }
    }

    public static String getHighestRemoteUid(final Account account, final String folderName) throws MessagingException, RakuRakuException {

        Folder remoteFolder = null;
        Store remoteStore;
        Message[] remoteMessageArray;
        ArrayList<String> allUidList = new ArrayList<String>();
        try {
            remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();
            if (remoteMessageCount > 0) {
                int remoteEnd = remoteMessageCount;
                remoteMessageArray = remoteFolder.getMessages(remoteEnd, remoteEnd, null, null);
                for (Message thisMessage : remoteMessageArray) {
                    allUidList.add(thisMessage.getUid());
                }
                remoteMessageArray = null;
            } else if (remoteMessageCount < 0) {
                throw new RakuRakuException("Message count " + remoteMessageCount + " for folder " + folderName);
            }
            if (!allUidList.isEmpty()) {
                return allUidList.get(0);
            } else {
                return null;
            }
        } finally {
            closeFolder(remoteFolder);
            remoteStore = null;
            remoteFolder = null;
            remoteMessageArray = null;
            allUidList = null;
        }
    }

    public static void synchronizeMailboxFinished(final Account account, final String folder) throws MessagingException {
        mListener.synchronizeMailboxFinished(account, folder, 0, 0);
    }

    public static void syncMailForAttachmentDownload(final Account account, final String folder, final MessageBean messageBean) throws MessagingException {
        if (isMessage(account, folder, messageBean.getUid()) && !SlideCheck.isDownloadedAttachment(messageBean)) {
            SlideAttachment.downloadAttachment(account, folder, messageBean.getUid());
        }
    }

    public static void syncMail(final Account account, final String folder, final String uid) throws MessagingException {
        SlideAttachment.downloadAttachment(account, folder, uid);
    }

    private static int getRemoteStart(int remoteMessageCount, int messageLimitCountFromRemote) {
        if (0 >= messageLimitCountFromRemote) {
            return 1;
        }
        int tmp = (remoteMessageCount - messageLimitCountFromRemote + 1);
        if (0 >= tmp) {
            return 1;
        } else {
            return tmp;
        }
    }

    /**
     * @param account    User Account Info(Account Class)
     * @param folderName Folder Name(String)
     * @throws MessagingException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static boolean isMessage(Account account, String folderName, String uid) throws MessagingException {

        LocalStore.LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            return localFolder.isMessage(uid);
        } finally {
            closeFolder(localFolder);
        }
    }

    public static int getRemoteMessageId(final Account account, final String folderName, final String uid) throws MessagingException, RakuRakuException {
        ArrayList<String> list = getRemoteUidList(account, folderName, 1, getRemoteMessageCount(account, folderName));
        return (list.indexOf(uid) + 1);
    }

    public static ArrayList<String> getRemoteUidList(final Account account, final String folderName, final int start, final int end) throws MessagingException, RakuRakuException {

        Folder remoteFolder = null;
        Message[] remoteMessages;
        ArrayList<String> result = new ArrayList<String>();
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);

            remoteMessages = remoteFolder.getMessages(start, end, null, null);
            for (Message message : remoteMessages) {
                result.add(message.getUid());
            }
            return result;

        } finally {
            closeFolder(remoteFolder);
            remoteFolder = null;
            remoteMessages = null;
            result = null;
        }
    }

    /**
     * @param account        User Account Info(Account Class)
     * @param folderName     Folder Name(String)
     * @param sentMessageUid sent message's UID
     * @throws MessagingException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void sentMessageAfter(Account account, String folderName, String sentMessageUid) throws MessagingException, RakuRakuException {
        if (account.getErrorFolderName().equals(folderName)) {
            return;
        }

        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;

        try {
            LocalStore localStore = account.getLocalStore();

            localFolder = localStore.getFolder(folderName);
            LocalStore.LocalMessage localMessage = (LocalStore.LocalMessage) localFolder
                    .getMessage(sentMessageUid);

            if (localMessage == null) {
                return;
            }

            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            if (!remoteFolder.exists()) {
                if (!remoteFolder.create(Folder.FolderType.HOLDS_MESSAGES)) {
                    return;
                }
            }

            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            if (remoteFolder.getMode() != Folder.OpenMode.READ_WRITE) {
            }

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            localFolder.fetch(new Message[]{localMessage}, fp, null);
            localMessage.setFlag(Flag.X_REMOTE_COPY_STARTED, true);

            remoteFolder.appendMessages(new Message[]{localMessage});

            localFolder.changeUid(localMessage);
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
    }

    //ローカルに落とした証でもマーキングしてんのか？
    private static void setLocalFlaggedCountToRemote(LocalStore.LocalFolder localFolder, Folder remoteFolder)
            throws MessagingException {

        int remoteFlaggedMessageCount = remoteFolder.getFlaggedMessageCount();
        if (remoteFlaggedMessageCount != -1) {
            localFolder.setFlaggedMessageCount(remoteFlaggedMessageCount);
        } else {
            int flaggedCount = 0;
            Message[] messages = localFolder.getMessages(null, false);
            for (Message message : messages) {
                if (message.isSet(Flag.FLAGGED) && !message.isSet(Flag.DELETED)) {
                    flaggedCount++;
                }
            }
            localFolder.setFlaggedMessageCount(flaggedCount);
        }
    }

    private static void closeFolder(Folder f) {
        if (f != null) {
            f.close();
        }
    }

    public static void addListener(MessagingListener listener) {
        mListener = listener;
        refreshListener(listener);
    }

    public static void refreshListener(MessagingListener listener) {
        if (memorizingListener != null && listener != null) {
            memorizingListener.refreshOther(listener);
        }
    }

    public static void removeCache(Account account, String folder, ArrayList<String> removeList) throws MessagingException, RakuRakuException {
        for (String uid : removeList) {
            SlideAttachment.clearCacheForAttachmentFile(account, folder, uid);
        }
    }

    /**
     * destroyはしないよ
     *
     * @param account
     * @param folderName
     * @param start
     * @param end
     */
    public static void syncMailbox(Account account, String folderName, int start, int end) throws MessagingException {
        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();

            if (remoteMessageCount > 0) {
                localFolder = account.getLocalStore().getFolder(folderName);
                localFolder.open(Folder.OpenMode.READ_WRITE);
                localFolder.updateLastUid();

                Message[] localMessages = localFolder.getMessages(null);
                HashMap<String, Message> localUidMap = new HashMap<String, Message>();
                for (Message message : localMessages) {
                    localUidMap.put(message.getUid(), message);
                }

                Message[] remoteMessageArray = null;
                remoteMessageArray = remoteFolder.getMessages(start, end, null, null);
                Message localMessage = null;

                for (Message thisMessage : remoteMessageArray) {
                    localMessage = localUidMap.get(thisMessage.getUid());
                    if (localMessage == null) {
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.BODY);
                        Message[] messageArray = new Message[]{thisMessage};
                        remoteFolder.fetch(messageArray, fp, null);

                        Message[] localMessageArray = null;
                        Message lMessage = null;
                        if (SlideCheck.isSlide(messageArray[0])) {
                            localFolder.appendMessages(messageArray);
                            fp.add(FetchProfile.Item.ENVELOPE);
                            lMessage = localFolder.getMessage(thisMessage.getUid());
                            localMessageArray = new Message[]{lMessage};
                            localFolder.fetch(localMessageArray, fp, null);
                            lMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);
                        }

                        messageArray = null;
                        localMessageArray = null;
                        fp = null;
                        lMessage = null;
                        localMessage = null;
                    }
                }
                remoteMessageArray = null;
                localMessages = null;
                localUidMap = null;

                localFolder.setLastChecked(System.currentTimeMillis());
                localFolder.setStatus(null);
            }
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
            remoteFolder = null;
            localFolder = null;
        }
    }

    public static int getRemoteMessageCount(Account account, String folderName) throws MessagingException {

        Folder remoteFolder = null;
        int result = 0;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            result = remoteFolder.getMessageCount();
        } finally {
            closeFolder(remoteFolder);
            remoteFolder = null;
        }
        return result;
    }
}
