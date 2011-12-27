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
    public static String synchronizeMailbox(Account account, String folderName) {
        Log.d("gunntama", "MessageSync#synchronizeMailbox start");

        String newMailUid = null;

        Folder remoteFolder = null;
        LocalStore.LocalFolder localFolder = null;
        try {
            //ローカル側の更新前データ保持
            localFolder = account.getLocalStore().getFolder(folderName);
            localFolder.open(Folder.OpenMode.READ_WRITE);
            localFolder.updateLastUid(); //こいつで一番最後のUIDを保持（localFolder.getLastUid();で取得）
            Log.d("gunntama", "MessageSync#synchronizeMailbox localFolder.getLastUid():" + localFolder.getLastUid());

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
                    Log.d("gunntama", "MessageSync#synchronizeMailbox thisMessage:" + thisMessage.getUid());
                    Message localMessage = localUidMap.get(thisMessage.getUid()); // このUIDは更新前もあるかなー？
                    remoteUidMap.put(thisMessage.getUid(), thisMessage); // 新規に増えたやつかな
                    if (localMessage == null) {
                        Log.d("gunntama", "MessageSync#synchronizeMailbox 新着メールのUID:" + thisMessage.getUid());
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.BODY);
                        remoteFolder.fetch(new Message[]{thisMessage}, fp, null);

                        localFolder.appendMessages(new Message[]{thisMessage});
                        fp.add(FetchProfile.Item.ENVELOPE);
                        Message lMessage = localFolder.getMessage(thisMessage.getUid());
                        localFolder.fetch(new Message[]{lMessage}, fp, null);

                        lMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);

                        newMailUid = thisMessage.getUid();
                        Log.d("gunntama", "MessageSync#synchronizeMailbox 新着メールのダウンロードおわった？");
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
                    Log.d("gunntama", "MessageSync#synchronizeMailbox さよならするメールのUID:" + localMessage.getUid());
                    Log.d("gunntama", "MessageSync#synchronizeMailbox さよならするメールの件名:" + localMessage.getSubject());
                    destroyMessages.add(localMessage);
                }
            }
            localFolder.destroyMessages(destroyMessages.toArray(new Message[0]));
            localMessages = null;

            setLocalFlaggedCountToRemote(localFolder, remoteFolder);

            localFolder.setLastChecked(System.currentTimeMillis());
            localFolder.setStatus(null);
            Log.d("gunntama", "MessageSync#synchronizeMailbox end");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
        return newMailUid;
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
