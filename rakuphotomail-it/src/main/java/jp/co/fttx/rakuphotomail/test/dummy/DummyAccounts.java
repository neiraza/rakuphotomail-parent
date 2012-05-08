package jp.co.fttx.rakuphotomail.test.dummy;

import android.content.Context;
import android.os.Bundle;
import jp.co.fttx.rakuphotomail.*;
import jp.co.fttx.rakuphotomail.activity.ActivityListener;
import jp.co.fttx.rakuphotomail.activity.GallerySlideShow;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;
import jp.co.fttx.rakuphotomail.activity.setup.AccountSetupBasics;
import jp.co.fttx.rakuphotomail.controller.MessagingController;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;


public class DummyAccounts extends RakuPhotoActivity {

    private Context mContext;
    private Account mAccount;

    ActivityListener mListener = new ActivityListener() {
        @Override
        public void informUserOfStatus() {
        }

        @Override
        public void folderStatusChanged(Account account, String folderName,
                                        int unreadMessageCount) {
        }

        @Override
        public void accountStatusChanged(BaseAccount account, AccountStats stats) {

        }

        @Override
        public void accountSizeChanged(Account account, long oldSize,
                                       long newSize) {
        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder,
                                               int totalMessagesInMailbox, int numNewMessages) {

            Account[] accounts = Preferences.getPreferences(mContext).getAccounts();
            GallerySlideShow.actionSlideShow(mContext, accounts[0], accounts[0].getInboxFolderName(), null);
            finish();
        }

        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            super.synchronizeMailboxStarted(account, folder);
        }

        @Override
        public void synchronizeMailboxFailed(Account account, String folder,
                                             String message) {
            super.synchronizeMailboxFailed(account, folder, message);

        }

    };

    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        mContext = this;
        setContentView(R.layout.accounts);
//        onAddNewAccount();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {

        super.onResume();
        MessagingController.getInstance(getApplication())
                .addListener(mListener);
        MessageSync.addListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        MessagingController.getInstance(getApplication()).removeListener(
                mListener);
    }

    private void onAddNewAccount() {
        AccountSetupBasics.actionNewAccount(this);
    }
}
