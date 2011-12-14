package jp.co.fttx.rakuphotomail.rakuraku.photomail;

import android.database.CursorIndexOutOfBoundsException;
import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.FetchProfile;
import jp.co.fttx.rakuphotomail.mail.Folder;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.UnavailableStorageException;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tooru.oguri
 * Date: 11/12/12
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class SlideMessage {
    private SlideMessage() {
        Log.d("maguro", "SlideMessage Construct");
    }

    /**
     * @param account
     * @param folder
     * @return
     */
    public static List<LocalStore.MessageInfo> getMessageInfoList(final Account account, final String folder) {
        Log.d("maguro", "SlideMessage#getMessageInfoList start");
        LocalStore localStore;
        LocalStore.LocalFolder localFolder = null;
        try {
            localStore = account.getLocalStore();
            localFolder = getLocalFolder(localStore, account, folder);
            Log.d("maguro", "SlideMessage#getMessageInfoList nomal end");
            return localStore.getMessages(localFolder.getId());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "SlideMessage#getMessageInfoList error:" + e);
        } finally {
            closeFolder(localFolder);
        }
        Log.d("maguro", "SlideMessage#getMessageInfoList abnomal end");
        return null;
    }

    /**
     * @param account
     * @param folder
     * @param uid
     * @return
     */
    public static LocalStore.LocalMessage getLocalMessage(final Account account, final String folder, final String uid) {
        Log.d("maguro", "SlideMessage#getLocalMessage start");
        LocalStore.LocalFolder localFolder = null;
        try {
            localFolder = getLocalFolder(null, account, folder);
            LocalStore.LocalMessage message = (LocalStore.LocalMessage) localFolder.getMessage(uid);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.BODY);
            localFolder.fetch(new Message[]{message}, fp, null);
            Log.d("maguro", "SlideMessage#getLocalMessage normal end");
            return message;
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        } finally {
            closeFolder(localFolder);
        }
        Log.d("maguro", "SlideMessage#getLocalMessage abnormal end");
        return null;
    }

    public static ArrayList<LocalStore.Attachments> getAttachmentList(final Account account, final String folder, final String uid) {
        Log.d("maguro", "SlideMessage#getAttachmentList start");
        LocalStore.LocalMessage message = getLocalMessage(account, folder, uid);
        LocalStore localStore = null;
        try {
            localStore = account.getLocalStore();
            Log.d("maguro", "SlideMessage#getAttachmentList normal end");
            return localStore.getAttachmentList(message.getId());
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "CursorIndexOutOfBoundsException:" + e);
        } catch (UnavailableStorageException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "UnavailableStorageException:" + e);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
        }
        Log.d("maguro", "SlideMessage#getAttachmentList abnormal end");
        return null;
    }

    private static LocalStore.LocalFolder getLocalFolder(LocalStore localStore, final Account account, final String folder) throws MessagingException {
        if (null == localStore) {
            localStore = account.getLocalStore();
        }
        LocalStore.LocalFolder localFolder = localStore.getFolder(folder);
        localFolder.open(Folder.OpenMode.READ_WRITE);
        return localFolder;
    }

    private static void closeFolder(Folder f) {
        Log.d("maguro", "SlideMessage#closeFolder start");
        if (f != null) {
            f.close();
        }
        Log.d("maguro", "SlideMessage#closeFolder end");
    }
}
