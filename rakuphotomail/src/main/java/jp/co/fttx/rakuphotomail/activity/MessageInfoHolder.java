package jp.co.fttx.rakuphotomail.activity;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import jp.co.fttx.rakuphotomail.helper.MessageHelper;
import jp.co.fttx.rakuphotomail.mail.Flag;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalMessage;

public class MessageInfoHolder {
    public String date;
    public Date compareDate;
    public String compareSubject;
    public CharSequence sender;
    public String senderAddress;
    public String compareCounterparty;
    public String[] recipients;
    public String uid;
    public boolean read;
    public boolean answered;
    public boolean flagged;
    public boolean downloaded;
    public boolean partially_downloaded;
    public boolean dirty;
    public LocalMessage message;
    public FolderInfoHolder folder;
    public boolean selected;
    public String account;
    public String uri;

    // Empty constructor for comparison
    public MessageInfoHolder() {
        this.selected = false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MessageInfoHolder == false) {
            return false;
        }
        MessageInfoHolder other = (MessageInfoHolder)o;
        return message.equals(other.message);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    public String getDate(MessageHelper messageHelper) {
        if (date == null) {
            date = messageHelper.formatDate(message.getSentDate());
        }
        return date;
    }
}
