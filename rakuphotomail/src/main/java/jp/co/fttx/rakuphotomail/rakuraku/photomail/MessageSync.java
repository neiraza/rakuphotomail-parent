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
     * @return String NewMail Uid
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static String syncMailboxForCheckNewMail(Account account, String folderName, int messageLimitCountFromRemote) {
        Log.d("pgr", "syncMailboxForCheckNewMail start");

        String newMailUid = null;

        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        try {
            //ローカル側の更新前データ保持
            localFolder = account.getLocalStore().getFolder(folderName);
            localFolder.open(Folder.OpenMode.READ_WRITE);
            localFolder.updateLastUid(); //こいつで一番最後のUIDを保持（localFolder.getLastUid();で取得）

            Message[] localMessages = localFolder.getMessages(null);
            HashMap<String, Message> localUidMap = new HashMap<String, Message>();
            for (Message message : localMessages) {
                Log.d("pgr", "syncMailboxForCheckNewMail message.getUid():" + message.getUid());
                localUidMap.put(message.getUid(), message);
            }

            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();
            Log.d("pgr", "syncMailboxForCheckNewMail remoteMessageCount:" + remoteMessageCount);

            Message[] remoteMessageArray = new Message[0];
            HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();

            //TODO ここに創意工夫をこらすか
            if (remoteMessageCount > 0) {
                int remoteStart = getRemoteStart(remoteMessageCount, messageLimitCountFromRemote);
                int remoteEnd = remoteMessageCount;
                Log.d("pgr", "syncMailboxForCheckNewMail remoteStart:" + remoteStart);
                Log.d("pgr", "syncMailboxForCheckNewMail remoteEnd:" + remoteEnd);

                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, null, null);
                Log.d("pgr", "syncMailboxForCheckNewMail remoteMessageArray:" + remoteMessageArray.length);

                //TODO ここでスライド対象外も全部DBに突っ込むと自滅するわな
                for (Message thisMessage : remoteMessageArray) {
                    Log.d("pgr", "syncMailboxForCheckNewMail チェック前 thisMessage:" + thisMessage.getUid());

                    Message localMessage = localUidMap.get(thisMessage.getUid()); // このUIDは更新前もあるかなー？
                    remoteUidMap.put(thisMessage.getUid(), thisMessage); // 新規に増えたやつかな
                    if (localMessage == null) {
                        Log.d("pgr", "syncMailboxForCheckNewMail 新規ぽい thisMessage:" + thisMessage.getUid());
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.BODY);
                        remoteFolder.fetch(new Message[]{thisMessage}, fp, null);
                        localFolder.appendMessages(new Message[]{thisMessage});
                        fp.add(FetchProfile.Item.ENVELOPE);
                        Message lMessage = localFolder.getMessage(thisMessage.getUid());
                        localFolder.fetch(new Message[]{lMessage}, fp, null);
                        lMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);
                        newMailUid = thisMessage.getUid();
                    }
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

        } catch (RakuRakuException re) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:RakuRakuException:" + re.getMessage());
            //タイムアウトも返してやろう作戦
            return null;
        } catch (MessagingException me) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:MessagingException:" + me.getMessage());
            return null;
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "ERROR:Exception:" + e.getMessage());
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
        return newMailUid;
    }

    public static boolean isSlideRemoteMail(final Account account, final String folder, final Message remoteMessage) throws MessagingException {
        Folder remoteFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folder);

            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            remoteFolder.fetch(new Message[]{remoteMessage}, fp, null);

            ArrayList<Part> Unnecessary = new ArrayList<Part>();
            ArrayList<Part> attachments = new ArrayList<Part>();
            MimeUtility.collectParts(remoteMessage, Unnecessary, attachments);

            for (Part attachment : attachments) {
                if (SlideCheck.isSlide(attachment)) {
                    return true;
                }
            }
            return false;
        } finally {
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
                Log.d("pgr", "syncMailbox message.getUid():" + message.getUid());

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
                //TODO sortせな

                remoteMessageArray = null;
            } else if (remoteMessageCount < 0) {
                throw new RakuRakuException("Message count " + remoteMessageCount + " for folder " + folderName);
            }
            return allMessageList;
        } finally {
            closeFolder(remoteFolder);
        }
    }

    public static String getHighestRemoteUid(final Account account, final String folderName) throws MessagingException, RakuRakuException {

        Folder remoteFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();
            Message[] remoteMessageArray;
            ArrayList<String> allUidList = new ArrayList<String>();
            if (remoteMessageCount > 0) {
                int remoteStart = 1;
                int remoteEnd = remoteMessageCount;
                //TODO 一件だけとかとれんかな
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
        }
    }

    public static void synchronizeMailboxFinished(final Account account, final String folder) throws MessagingException {
        Log.d("ahokato", "MessageSync#synchronizeMailboxFinished start");
        mListener.synchronizeMailboxFinished(account, folder, 0, 0);
    }

    public static void syncMailUseDelegate(final Account account, final String folder, final Message remoteMessage) throws MessagingException {
        Log.d("ahokato", "MessageSync#syncMailUseDelegate start");

        if (!isMessage(account, folder, remoteMessage.getUid())) {
            SlideAttachment.downloadAttachment(account, folder, remoteMessage);
            Log.d("ahokato", "MessageSync#syncMailUseDelegate SlideAttachment#downloadAttachment end");

            Log.d("ahokato", "MessageSync#syncMailUseDelegate synchronizeMailboxFinished");
            mListener.synchronizeMailboxFinished(account, folder, 0, 0);
        }
    }

    public static void syncMail(final Account account, final String folder, final Message remoteMessage) throws MessagingException {
        Log.d("ahokato", "MessageSync#syncMail start");

        if (!isMessage(account, folder, remoteMessage.getUid())) {
            SlideAttachment.downloadAttachment(account, folder, remoteMessage);
        }
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
}
