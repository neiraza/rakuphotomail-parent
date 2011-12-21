/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.*;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class MessageSync {

    // こいつを解析する
    public static void synchronizeMailbox(Account account, String folderName) {
        Log.d("maguro", "MessageSync#synchronizeMailbox start");

        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        try {
            //ローカル側の更新前データ保持
            localFolder = account.getLocalStore().getFolder(folderName);
            localFolder.open(Folder.OpenMode.READ_WRITE);
            localFolder.updateLastUid(); //こいつで一番最後のUIDを保持（localFolder.getLastUid();で取得）
            Log.d("maguro", "MessageSync#synchronizeMailbox localFolder.getLastUid():" + localFolder.getLastUid());

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
            final ArrayList<Message> remoteMessages = new ArrayList<Message>();
            HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();
            final Date earliestDate = account.getEarliestPollDate();
            if (remoteMessageCount > 0) {
                int remoteStart = 1;
                int remoteEnd = remoteMessageCount;
                final AtomicInteger headerProgress = new AtomicInteger(0); // 使わない？
                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, earliestDate, null);
                for (Message thisMess : remoteMessageArray) {
                    headerProgress.incrementAndGet(); // 使わない？
                    Message localMessage = localUidMap.get(thisMess.getUid()); // このUIDは更新前もあるかなー？
                    if (localMessage == null || !localMessage.olderThan(earliestDate)) {
                        remoteMessages.add(thisMess); // 新規に増えたやつかな, 使いどころがみえないな
                        remoteUidMap.put(thisMess.getUid(), thisMess); // 新規に増えたやつかな
                        Log.d("maguro", "MessageSync#synchronizeMailbox 新メールのUID:" + thisMess.getUid());
                        Log.d("maguro", "MessageSync#synchronizeMailbox 新メールの件名:" + thisMess.getSubject());
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
                    Log.d("maguro", "MessageSync#synchronizeMailbox さよならするメールのUID:" + localMessage.getUid());
                    Log.d("maguro", "MessageSync#synchronizeMailbox さよならするメールの件名:" + localMessage.getSubject());
                    destroyMessages.add(localMessage);
                }
            }
            localFolder.destroyMessages(destroyMessages.toArray(new Message[0]));
            localMessages = null;

            setLocalFlaggedCountToRemote(localFolder, remoteFolder);

            localFolder.setLastChecked(System.currentTimeMillis());
            localFolder.setStatus(null);
            Log.d("maguro", "MessageSync#synchronizeMailbox end");

        } catch (Exception e) {
            e.printStackTrace();
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
