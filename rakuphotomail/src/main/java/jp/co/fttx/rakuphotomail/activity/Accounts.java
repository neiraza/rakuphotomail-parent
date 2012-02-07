package jp.co.fttx.rakuphotomail.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import jp.co.fttx.rakuphotomail.*;
import jp.co.fttx.rakuphotomail.activity.setup.AccountSetupBasics;
import jp.co.fttx.rakuphotomail.controller.MessagingController;


public class Accounts extends RakuPhotoActivity implements OnClickListener {

    private Context mContext;

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
            startActivity(new Intent(mContext, Accounts.class));
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
        Account[] accounts = Preferences.getPreferences(this).getAccounts();
        if (accounts.length == 1 && accounts[0].isAvailable(this)) {
            GallerySlideShow.actionSlideShow(this, accounts[0], accounts[0].getInboxFolderName(), null);
            finish();
        }

        findViewById(R.id.next).setOnClickListener(this);
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

    public void onClick(View view) {
        onAddNewAccount();
    }
}
