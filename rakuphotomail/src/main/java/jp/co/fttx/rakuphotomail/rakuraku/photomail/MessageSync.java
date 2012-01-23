/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.mail.FetchProfile;
import jp.co.fttx.rakuphotomail.mail.Flag;
import jp.co.fttx.rakuphotomail.mail.Folder;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Store;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class MessageSync {

    // こいつを解析する
    //TODO 一件しか取得できない？？

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
    public static String synchronizeMailbox(Account account, String folderName) {
        Log.d("refs2608@", "MessageSync#synchronizeMailbox start folderName:"+folderName);

        String newMailUid = null;

        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        try {
            //ローカル側の更新前データ保持
            localFolder = account.getLocalStore().getFolder(folderName);
            localFolder.open(Folder.OpenMode.READ_WRITE);
            localFolder.updateLastUid(); //こいつで一番最後のUIDを保持（localFolder.getLastUid();で取得）
            Log.d("refs2608", "MessageSync#synchronizeMailbox localFolder.getLastUid():" + localFolder.getLastUid());

            Message[] localMessages = localFolder.getMessages(null);
            HashMap<String, Message> localUidMap = new HashMap<String, Message>();
            for (Message message : localMessages) {
                localUidMap.put(message.getUid(), message);
            }

            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            int remoteMessageCount = remoteFolder.getMessageCount();

            Message[] remoteMessageArray = new Message[0];
            HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();

            if (remoteMessageCount > 0) {
                int remoteStart = 1;
                int remoteEnd = remoteMessageCount;
                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, null, null);

                for (Message thisMessage : remoteMessageArray) {
                    Log.d("refs2608", "MessageSync#synchronizeMailbox thisMessage:" + thisMessage.getUid());
                    Message localMessage = localUidMap.get(thisMessage.getUid()); // このUIDは更新前もあるかなー？
                    remoteUidMap.put(thisMessage.getUid(), thisMessage); // 新規に増えたやつかな
                    if (localMessage == null) {
                        Log.d("refs2608", "MessageSync#synchronizeMailbox 新着メールのUID:" + thisMessage.getUid());
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.BODY);
                        remoteFolder.fetch(new Message[]{thisMessage}, fp, null);

                        localFolder.appendMessages(new Message[]{thisMessage});
                        fp.add(FetchProfile.Item.ENVELOPE);
                        Message lMessage = localFolder.getMessage(thisMessage.getUid());
                        localFolder.fetch(new Message[]{lMessage}, fp, null);

                        lMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);

                        newMailUid = thisMessage.getUid();
                        Log.d("refs2608", "MessageSync#synchronizeMailbox 新着メールのダウンロードおわった？");
                    }
                }
                remoteMessageArray = null;
            } else if (remoteMessageCount < 0) {
                throw new Exception("Message count " + remoteMessageCount + " for folder " + folderName);
            }

            //古いメッセージを消してるぽい
            ArrayList<Message> destroyMessages = new ArrayList<Message>();
            for (Message localMessage : localMessages) {
                if (remoteUidMap.get(localMessage.getUid()) == null) {
                    Log.d("refs2608", "MessageSync#synchronizeMailbox さよならするメールのUID:" + localMessage.getUid());
                    Log.d("refs2608", "MessageSync#synchronizeMailbox さよならするメールの件名:" + localMessage.getSubject());
                    destroyMessages.add(localMessage);
                }
            }
            localFolder.destroyMessages(destroyMessages.toArray(new Message[0]));
            localMessages = null;

            setLocalFlaggedCountToRemote(localFolder, remoteFolder);

            localFolder.setLastChecked(System.currentTimeMillis());
            localFolder.setStatus(null);
            Log.d("refs2608@", "MessageSync#synchronizeMailbox end");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
        return newMailUid;
    }

    /**
     * @param account        User Account Info(Account Class)
     * @param folderName     Folder Name(String)
     * @param sentMessageUid sent message's UID
     * @throws MessagingException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void sentMessageAfter(Account account, String folderName, String sentMessageUid) throws MessagingException {
        Log.d("refs1961", "MessageSync#sentMessageAfter");

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
                Log.d("refs1961", "MessageSync#sentMessageAfter localMessage is null");
                return;
            }

            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folderName);
            Log.d("refs1961", "MessageSync#sentMessageAfter !remoteFolder.exists():"+!remoteFolder.exists());
            if (!remoteFolder.exists()) {
                if (!remoteFolder.create(Folder.FolderType.HOLDS_MESSAGES)) {
                    Log.d("refs1961", "MessageSync#sentMessageAfter FolderType?");
                    return;
                }
            }

            remoteFolder.open(Folder.OpenMode.READ_WRITE);
            if (remoteFolder.getMode() != Folder.OpenMode.READ_WRITE) {
                Log.d("refs1961", "MessageSync#sentMessageAfter remoteFolder.getMode():"+remoteFolder.getMode());
                return;
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
        Log.d("maguro", "MessageSync#setLocalFlaggedCountToRemote start");

        int remoteFlaggedMessageCount = remoteFolder.getFlaggedMessageCount();
        if (remoteFlaggedMessageCount != -1) {
            Log.d("maguro", "MessageSync#setLocalFlaggedCountToRemote setFlaggedMessageCountって何だよwwwwwwwwwww");
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
        Log.d("maguro", "MessageSync#setLocalFlaggedCountToRemote end");
    }

    private static void closeFolder(Folder f) {
        if (f != null) {
            f.close();
        }
    }
}
