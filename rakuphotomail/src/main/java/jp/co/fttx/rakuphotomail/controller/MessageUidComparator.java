package jp.co.fttx.rakuphotomail.controller;

import jp.co.fttx.rakuphotomail.mail.Message;

import java.util.Comparator;

public class MessageUidComparator implements Comparator<Message> {
    public int compare(Message o1, Message o2) {
        try {
            if (o1.getUid() == null) {
                return 1;
            } else if (o2.getUid() == null) {
                return -1;
            } else
            return o2.getUid().compareTo(o1.getUid());
        } catch (Exception e) {
            return 0;
        }
    }
}
