
package jp.co.fttx.rakuphotomail.mail.transport;

import android.util.Log;

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Transport;
import jp.co.fttx.rakuphotomail.mail.store.WebDavStore;

public class WebDavTransport extends Transport {
    private WebDavStore store;

    public WebDavTransport(Account account) throws MessagingException {
        if (account.getRemoteStore() instanceof WebDavStore) {
            store = (WebDavStore) account.getRemoteStore();
        } else {
            store = new WebDavStore(account);
        }

        if (RakuPhotoMail.DEBUG)
            Log.d(RakuPhotoMail.LOG_TAG, ">>> New WebDavTransport creation complete");
    }

    @Override
    public void open() throws MessagingException {
        if (RakuPhotoMail.DEBUG)
            Log.d(RakuPhotoMail.LOG_TAG, ">>> open called on WebDavTransport ");

        store.getHttpClient();
    }

    @Override
    public void close() {
    }

    @Override
    public void sendMessage(Message message) throws MessagingException {
        store.sendMessages(new Message[] { message });
    }
}
