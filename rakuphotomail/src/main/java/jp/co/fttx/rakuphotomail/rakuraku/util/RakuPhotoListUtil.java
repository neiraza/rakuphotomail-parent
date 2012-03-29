package jp.co.fttx.rakuphotomail.rakuraku.util;

import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.controller.UidComparator;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: tooru.oguri
 * Date: 12/03/05
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class RakuPhotoListUtil {
    public static boolean isUidForMessage(ArrayList<Message> messages, String uid) {
        for (Message message : messages) {
            if (uid.equals(message.getUid())) {
                return true;
            }
        }
        return false;
    }

    public static String getUidForMessage(ArrayList<Message> messages, String uid) {
        for (Message message : messages) {
            if (uid.equals(message.getUid())) {
                return message.getUid();
            }
        }
        return null;
    }

    public static int getIndexForMessage(ArrayList<Message> messages, String uid) {
        for (int i = 0; i < messages.size(); i++) {
            if (uid.equals(messages.get(i).getUid())) {
                return i;
            }
        }
        return 0;
    }

    public static String getPreUid(ArrayList<Message> messages, String uid) {
        for (int i = 0; i < messages.size(); i++) {
            if (uid.equals(messages.get(i).getUid())) {
                if (i + 1 < messages.size()) {
                    return messages.get(i + 1).getUid();
                }
            }
        }
        return null;
    }

    public static ArrayList<String> getNewUidList(ArrayList<Message> oldList, ArrayList<Message> newList) throws MessagingException {
        Log.d("ahokato", "GallerySlideShow#getNewUidList start");

        ArrayList<String> resultUid = new ArrayList<String>();
        for (Message newMessage : newList) {
            String newUid = newMessage.getUid();
            boolean is = false;
            for (Message oldMessage : oldList) {
                if (newUid.equals(oldMessage.getUid())) {
                    is = true;
                }
            }
            if (!is) {
                resultUid.add(newUid);
            }
        }
        Collections.sort(resultUid, new UidComparator(UidComparator.DESC));
        return resultUid;
    }

    public static String getNewUid(Account account, String folder, ArrayList<String> resultUid, ArrayList<Message> newList) throws MessagingException {
        if (0 < resultUid.size()) {
            for (String newUid : resultUid) {
                Message resultMessage = null;
                for (Message newMessage : newList) {
                    if (newUid.equals(newMessage.getUid())) {
                        resultMessage = newMessage;
                    }
                }
                if (null != resultMessage && MessageSync.isSlideRemoteMail(account, folder, resultMessage)) {
                    return newUid;
                }
            }
        }
        return null;
    }
}
