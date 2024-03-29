package jp.co.fttx.rakuphotomail.helper;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.activity.FolderInfoHolder;
import jp.co.fttx.rakuphotomail.activity.MessageInfoHolder;
import jp.co.fttx.rakuphotomail.mail.Address;
import jp.co.fttx.rakuphotomail.mail.Flag;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Message.RecipientType;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalMessage;
import jp.co.fttx.rakuphotomail.helper.DateFormatter;

public class MessageHelper {

    private static MessageHelper sInstance;

    public synchronized static MessageHelper getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new MessageHelper(context);
        }
        return sInstance;
    }

    private Context mContext;

    private DateFormat mTodayDateFormat;

    private DateFormat mDateFormat;

    private MessageHelper(final Context context) {
        mContext = context;
        mDateFormat = DateFormatter.getDateFormat(mContext);
        mTodayDateFormat = android.text.format.DateFormat.getTimeFormat(mContext);
    }

    public void populate(final MessageInfoHolder target, final Message m,
                         final FolderInfoHolder folder, final Account account) {
        final Contacts contactHelper = RakuPhotoMail.showContactName() ? Contacts.getInstance(mContext) : null;
        try {
            LocalMessage message = (LocalMessage) m;
            target.message = message;
            target.compareDate = message.getSentDate();
            if (target.compareDate == null) {
                target.compareDate = message.getInternalDate();
            }

            target.folder = folder;

            target.read = message.isSet(Flag.SEEN);
            target.answered = message.isSet(Flag.ANSWERED);
            target.flagged = message.isSet(Flag.FLAGGED);
            target.downloaded = message.isSet(Flag.X_DOWNLOADED_FULL);
            target.partially_downloaded = message.isSet(Flag.X_DOWNLOADED_PARTIAL);

            Address[] addrs = message.getFrom();

            if (addrs.length > 0 &&  account.isAnIdentity(addrs[0])) {
                CharSequence to = Address.toFriendly(message .getRecipients(RecipientType.TO), contactHelper);
                target.compareCounterparty = to.toString();
                target.sender = new SpannableStringBuilder(mContext.getString(R.string.message_to_label)).append(to);
            } else {
                target.sender = Address.toFriendly(addrs, contactHelper);
                target.compareCounterparty = target.sender.toString();
            }

            if (addrs.length > 0) {
                target.senderAddress = addrs[0].getAddress();
            } else {
                // a reasonable fallback "whomever we were corresponding with
                target.senderAddress = target.compareCounterparty;
            }




            target.uid = message.getUid();

            target.account = account.getDescription();
            target.uri = "email://messages/" + account.getAccountNumber() + "/" + m.getFolder().getName() + "/" + m.getUid();

        } catch (MessagingException me) {
            Log.w(RakuPhotoMail.LOG_TAG, "Unable to load message info", me);
        }
    }
    public String formatDate(Date date) {
        if (Utility.isDateToday(date)) {
            return mTodayDateFormat.format(date);
        } else {
            return mDateFormat.format(date);
        }
    }

    public void refresh() {
        mDateFormat = DateFormatter.getDateFormat(mContext);
        mTodayDateFormat = android.text.format.DateFormat.getTimeFormat(mContext);
    }
}
