package jp.co.fttx.rakuphotomail.controller;

import android.util.Log;
import jp.co.fttx.rakuphotomail.mail.Message;

import java.util.Comparator;

public class MessageUidComparator implements Comparator<Message> {
    public int compare(Message o1, Message o2) {
        Log.d("ahokato", "MessageUidComparator#compare");
        Log.d("ahokato", "MessageUidComparator#compare :" + o1.getUid());
        Log.d("ahokato", "MessageUidComparator#compare :" + o2.getUid());
        try {
            if (o1.getUid() == null) {
                Log.d("ahokato", "MessageUidComparator#compare return 1");
                return 1;
            } else if (o2.getUid() == null) {
                return -1;
            } else
                Log.d("ahokato", "MessageUidComparator#compare return o2.getUid().compareTo(o1.getUid():" + o2.getUid().compareTo(o1.getUid()));
            return o2.getUid().compareTo(o1.getUid());
        } catch (Exception e) {
            Log.d("ahokato", "MessageUidComparator#compare return 0");
            return 0;
        }
    }
}
