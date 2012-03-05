package jp.co.fttx.rakuphotomail.rakuraku.util;

import jp.co.fttx.rakuphotomail.mail.Message;

import java.util.ArrayList;

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

}
