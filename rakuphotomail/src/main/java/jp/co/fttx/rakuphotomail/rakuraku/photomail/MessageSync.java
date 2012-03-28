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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class MessageSync {

    private static Set<MessagingListener> mListeners = new CopyOnWriteArraySet<MessagingListener>();
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

    public static ArrayList<String> isSlideRemoteMailList(final Account account, final String folder, final Message[] remoteMessages) throws MessagingException {
        Log.d("ahokato", "MessageSync#isSlideRemoteMailList start");

        Folder remoteFolder = null;
        FetchProfile fp = null;
        ArrayList<Part> Unnecessary = null;
        ArrayList<Part> attachments = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folder);

            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            remoteFolder.fetch(remoteMessages, fp, null);

            ArrayList<String> result = new ArrayList<String>();

            for (Message remoteMessage : remoteMessages) {
                Log.d("ahokato", "MessageSync#isSlideRemoteMailList remoteMessage:" + remoteMessage.getUid());
                Unnecessary = new ArrayList<Part>();
                attachments = new ArrayList<Part>();
                MimeUtility.collectParts(remoteMessage, Unnecessary, attachments);
                for (Part attachment : attachments) {
                    if (SlideCheck.isSlide(attachment)) {
                        Log.d("ahokato", "MessageSync#isSlideRemoteMailList add result:" + remoteMessage.getUid());
                        result.add(remoteMessage.getUid());
                    }
                }
            }
            return result;
        } finally {
            fp = null;
            Unnecessary = null;
            attachments = null;
            closeFolder(remoteFolder);
            remoteFolder = null;
        }
    }

    public static boolean isSlideRemoteMail(final Account account, final String folder, final Message remoteMessage) throws MessagingException {
        Log.d("ahokato", "MessageSync#isSlideRemoteMail start");

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

    public static void syncMailbox(Account account, String folderName, int messageLimitCountFromRemote) {
        Log.d("pgr", "syncMailbox start");

        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        try {
            localFolder = account.getLocalStore().getFolder(folderName);
            localFolder.open(Folder.OpenMode.READ_WRITE);
            localFolder.updateLastUid();

            Message[] localMessages = localFolder.getMessages(null);
            HashMap<String, Message> localUidMap = new HashMap<String, Message>();
            for (Message message : localMessages) {
                Log.d("pgr", "syncMailbox message.getRemoteUid():" + message.getUid());

                //TODO これ使ってなくね？
                localUidMap.put(message.getUid(), message);
            }

            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();
            Log.d("pgr", "syncMailbox remoteMessageCount:" + remoteMessageCount);

            Message[] remoteMessageArray = new Message[0];
            HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();

            if (remoteMessageCount > 0) {
                int remoteStart = getRemoteStart(remoteMessageCount, messageLimitCountFromRemote);
                int remoteEnd = remoteMessageCount;
                Log.d("pgr", "syncMailbox remoteStart:" + remoteStart);
                Log.d("pgr", "syncMailbox remoteEnd:" + remoteEnd);
                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, null, null);

                Log.d("pgr", "syncMailbox remoteMessageArray:" + remoteMessageArray.length);

                for (Message thisMessage : remoteMessageArray) {
                    Log.d("pgr", "syncMailbox thisMessage:" + thisMessage.getUid());

                    remoteUidMap.put(thisMessage.getUid(), thisMessage);
                }
                remoteMessageArray = null;
            } else if (remoteMessageCount < 0) {
                throw new Exception("Message count " + remoteMessageCount + " for folder " + folderName);
            }
            Log.d("pgr", "syncMailboxForCheckNewMail あとはdestroyMessagesとかしておわりなので割愛");

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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
    }

    public static ArrayList<Message> getRemoteMessage(final Account account, final String folderName, final ArrayList<String> uids) throws MessagingException, RakuRakuException {

        Folder remoteFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();
            Message[] remoteMessageArray;
            ArrayList<Message> messageList = new ArrayList<Message>();
            if (remoteMessageCount > 0) {
                remoteMessageArray = remoteFolder.getMessages(uids.toArray(new String[uids.size()]), null);
                for (Message thisMessage : remoteMessageArray) {
                    messageList.add(thisMessage);
                }
                remoteMessageArray = null;
            } else if (remoteMessageCount < 0) {
                throw new RakuRakuException("Message count " + remoteMessageCount + " for folder " + folderName);
            }
            return messageList;
        } finally {
            closeFolder(remoteFolder);
        }
    }

    public static ArrayList<Message> getRemoteAllMessage(final Account account, final String folderName) throws MessagingException, RakuRakuException {

        Folder remoteFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();
            Message[] remoteMessageArray;
            ArrayList<Message> allMessageList = new ArrayList<Message>();
            if (remoteMessageCount > 0) {
                int remoteStart = 1;
                int remoteEnd = remoteMessageCount;
                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, null, null);
                for (Message thisMessage : remoteMessageArray) {
                    allMessageList.add(thisMessage);
                }
                remoteMessageArray = null;
            } else if (remoteMessageCount < 0) {
                throw new RakuRakuException("Message count " + remoteMessageCount + " for folder " + folderName);
            }
            return allMessageList;
        } finally {
            closeFolder(remoteFolder);
        }
    }

//    public static ArrayList<String> getRemoteUidSince(final Account account, final String folderName, final long date) throws MessagingException, RakuRakuException {
//        Folder remoteFolder = null;
//        try {
//            Store remoteStore = account.getRemoteStore();
//            remoteFolder = remoteStore.getFolder(folderName);
//            remoteFolder.open(Folder.OpenMode.READ_WRITE);
//            int remoteMessageCount = remoteFolder.getMessageCount();
//            Message[] remoteMessageArray;
//            ArrayList<String> result = new ArrayList<String>();
//            if (remoteMessageCount > 0) {
//                int remoteStart = 1;
//                int remoteEnd = remoteMessageCount;
//                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, null, null);
//                for (Message thisMessage : remoteMessageArray) {
//                    result.add(thisMessage.getRemoteUid());
//                }
//                remoteMessageArray = null;
//            } else if (remoteMessageCount < 0) {
//                throw new RakuRakuException("Message count " + remoteMessageCount + " for folder " + folderName);
//            }
//            return result;
//        } finally {
//            closeFolder(remoteFolder);
//        }
//    }

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
        Log.d("ahokato", "MessageSync#synchronizeMailboxFinished start");
        mListener.synchronizeMailboxFinished(account, folder, 0, 0);
    }

//    public static void syncMailUseDelegate(final Account account, final String folder, final Message remoteMessage) throws MessagingException {
//        Log.d("ahokato", "MessageSync#syncMailUseDelegate start");
//
//        if (!isMessage(account, folder, remoteMessage.getRemoteUid())) {
//            SlideAttachment.downloadAttachment(account, folder, remoteMessage);
//            Log.d("ahokato", "MessageSync#syncMailUseDelegate SlideAttachment#downloadAttachment end");
//
//            Log.d("ahokato", "MessageSync#syncMailUseDelegate synchronizeMailboxFinished");
//            mListener.synchronizeMailboxFinished(account, folder, 0, 0);
//        }
//    }

    public static void syncMailForAttachmentDownload(final Account account, final String folder, final MessageBean messageBean) throws MessagingException {
        Log.d("ahokato", "MessageSync#syncMail(final Account account, final String folder, final MessageBean messageBean)");
        if (isMessage(account, folder, messageBean.getUid()) && !SlideCheck.isDownloadedAttachment(messageBean)) {
            SlideAttachment.downloadAttachment(account, folder, messageBean.getUid());
        }
    }

    public static void syncMail(final Account account, final String folder, final String uid) throws MessagingException {
        Log.d("ahokato", "MessageSync#syncMail(final Account account, final String folder, final String uid) uid:" + uid);
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
        Log.d("ahokato", "MessageSync#isMessage uid:" + uid);

        LocalStore.LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            return localFolder.isMessage(uid);
        } finally {
            closeFolder(localFolder);
        }
    }

    public static Message getRemoteMessage(final Account account, final String folderName, final String uid) throws MessagingException, RakuRakuException {

        Folder remoteFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);

            Message remoteMessage = null;
            if (null != uid) {
                remoteMessage = remoteFolder.getMessage(uid);
            }
            return remoteMessage;

        } finally {
            closeFolder(remoteFolder);
        }
    }

    public static int getRemoteMessageId(final Account account, final String folderName, final String uid) throws MessagingException, RakuRakuException {
        Log.d("ahokato", "MessageSync#getRemoteMessageId uid:" + uid);
        ArrayList<String> list = getRemoteUidList(account, folderName, 1, getRemoteMessageCount(account, folderName));
        return (list.indexOf(uid) + 1);
    }

    public static ArrayList<String> getRemoteUidList(final Account account, final String folderName, final int start, final int end) throws MessagingException, RakuRakuException {
        Log.d("ahokato", "MessageSync#getRemoteUidList:" + start + "-" + end);

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

    public static void addListeners(MessagingListener listener) {
        mListeners.add(listener);
        refreshListener(listener);
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

    public static void removeListeners(MessagingListener listener) {
        mListeners.remove(listener);
    }

    public static void removeListener(MessagingListener listener) {
        mListener = null;
    }

    public static Set<MessagingListener> getListeners(MessagingListener listener) {
        if (listener == null) {
            return mListeners;
        }

        Set<MessagingListener> listeners = new HashSet<MessagingListener>(
                mListeners);
        listeners.add(listener);
        return listeners;
    }

    /**
     * [Account#getAttachmentCacheLimitCount() / 2] Cache Delete
     *
     * @param account user account
     * @param folder  imap folder
     * @param dispUid current dispUid
     * @throws MessagingException me
     * @throws RakuRakuException  rre
     */
    public static void removeCache(Account account, String folder, String dispUid) throws MessagingException, RakuRakuException {
        Log.d("ahokato", "MessageSync#removeCache start");
        ArrayList<String> downloadedList = SlideMessage.getMessageUidRemoveTarget(account);
        if (downloadedList.size() > account.getAttachmentCacheLimitCount()) {
            int removeCount = account.getAttachmentCacheLimitCount() / 2;
            Log.d("ahokato", "MessageSync#removeCache removeCount:" + removeCount);
            int currentIndex = downloadedList.indexOf(dispUid);
            Log.d("ahokato", "MessageSync#removeCache currentIndex:" + currentIndex);
            ArrayList<String> removeList = createRemoveList(downloadedList, currentIndex, removeCount);
            for (String uid : removeList) {
                Log.d("ahokato", "MessageSync#removeCache uid:" + uid);
                SlideAttachment.clearCacheForAttachmentFile(account, folder, uid);
            }
        }
    }

    public static void removeCache(Account account, String folder, ArrayList<String> removeList) throws MessagingException, RakuRakuException {
        Log.d("ahokato", "MessageSync#removeCache start");
        for (String uid : removeList) {
            Log.d("ahokato", "MessageSync#removeCache uid:" + uid);
            SlideAttachment.clearCacheForAttachmentFile(account, folder, uid);
        }
    }

    /**
     * [All] Cache Delete
     *
     * @param account user account
     * @param folder  imap folder
     * @throws MessagingException me
     * @throws RakuRakuException  rre
     */
    public static void removeAllCache(Account account, String folder) throws MessagingException, RakuRakuException {
        ArrayList<String> downloadedList = SlideMessage.getMessageUidRemoveTarget(account);
        for (String uid : downloadedList) {
            SlideAttachment.clearCacheForAttachmentFile(account, folder, uid);
        }
    }

    private static ArrayList<String> createRemoveList(ArrayList<String> src, int currentIndex, int removeCount) {
        ArrayList<String> dest = new ArrayList<String>();
        if (0 == currentIndex) {
            //あるばあい リストの先頭で１つ後ろがないので逆にリストの後ろを消していく
            for (int i = (src.size() - 1); i >= (src.size() - removeCount); i--) {
                dest.add(src.get(i));
            }
        } else if (0 < currentIndex) {
            //あるばあい
            if (currentIndex >= removeCount) {
                //currentIndex-1 から -removeCount 件を削除リストにつっこむ
                for (int i = (currentIndex - 1); i >= (currentIndex - removeCount); i--) {
                    dest.add(src.get(i));
                }
            } else {
                System.out.println(Math.abs(currentIndex - removeCount));
                //abs(removeCount) の分だけ後ろからもってくる
                for (int i = (src.size() - 1); i >= (src.size() - Math.abs(currentIndex - removeCount)); i--) {
                    dest.add(src.get(i));
                }
                // currentIndex-1 から 先頭まで削除
                for (int i = (currentIndex - 1); i >= 0; i--) {
                    dest.add(src.get(i));
                }
            }
        } else {
            //ないばあい リストの一番後ろから件数分さくじょ
            for (int i = (src.size() - 1); i >= (src.size() - removeCount); i--) {
                dest.add(src.get(i));
            }
        }
        return dest;
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
        Log.d("ahokato", "MessageSync#syncMailbox start");

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
                Log.d("ahokato", "MessageSync#syncMailbox remoteMessageArray:" + remoteMessageArray.length);
                Message localMessage = null;

                for (Message thisMessage : remoteMessageArray) {
                    Log.d("ahokato", "MessageSync#syncMailbox thisMessage:" + thisMessage.getUid());
                    localMessage = localUidMap.get(thisMessage.getUid());
                    if (localMessage == null) {
                        Log.d("ahokato", "MessageSync#syncMailbox new mail:" + thisMessage.getUid());
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.BODY);
                        Message[] messageArray = new Message[]{thisMessage};
                        remoteFolder.fetch(messageArray, fp, null);

                        Message[] localMessageArray = null;
                        Message lMessage = null;
                        if (SlideCheck.isSlide(messageArray[0])) {
                            Log.d("ahokato", "MessageSync#syncMailbox Slide対象:" + thisMessage.getUid());
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
                Log.d("ahokato", "MessageSync#syncMailbox end");
            }
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
            remoteFolder = null;
            localFolder = null;
        }
    }

    public static int getRemoteMessageCount(Account account, String folderName) throws MessagingException {
        Log.d("ahokato", "MessageSync#getRemoteMessageCount start");

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
