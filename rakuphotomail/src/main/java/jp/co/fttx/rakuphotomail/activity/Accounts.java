package jp.co.fttx.rakuphotomail.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
            Log.d("refs#2169@@", "ActivityListener#informUserOfStatus");
        }

        @Override
        public void folderStatusChanged(Account account, String folderName,
                                        int unreadMessageCount) {
            Log.d("refs#2169@@", "ActivityListener#folderStatusChanged");
        }

        @Override
        public void accountStatusChanged(BaseAccount account, AccountStats stats) {
            Log.d("refs#2169@@", "ActivityListener#accountStatusChanged");

        }

        @Override
        public void accountSizeChanged(Account account, long oldSize,
                                       long newSize) {
            Log.d("refs#2169@@", "ActivityListener#accountSizeChanged");
        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder,
                                               int totalMessagesInMailbox, int numNewMessages) {
            Log.d("refs#2169@@", "ActivityListener#synchronizeMailboxFinished");

            startActivity(new Intent(mContext, Accounts.class));
            finish();
        }

        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            Log.d("refs#2169@@", "ActivityListener#synchronizeMailboxStarted");

            super.synchronizeMailboxStarted(account, folder);
        }

        @Override
        public void synchronizeMailboxFailed(Account account, String folder,
                                             String message) {
            Log.d("refs#2169@@", "ActivityListener#synchronizeMailboxFailed");

            super.synchronizeMailboxFailed(account, folder, message);

        }

    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d("refs#2169@@", "Accounts#onCreate");
        mContext = this;
        setContentView(R.layout.accounts);
        Account[] accounts = Preferences.getPreferences(this).getAccounts();
        if (accounts.length == 1 && accounts[0].isAvailable(this)) {
            Log.d("refs#2169@@", "Accounts#onCreate アカウントあるからスライドいくぜー");
            GallerySlideShow.actionSlideShow(this, accounts[0], accounts[0].getInboxFolderName(), null);
            finish();
        }

        findViewById(R.id.next).setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("refs#2169@@", "Accounts#onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.d("refs#2169@@", "Accounts#onResume");

        super.onResume();
        MessagingController.getInstance(getApplication())
                .addListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("fujiyama", "Accounts#onPause");

        MessagingController.getInstance(getApplication()).removeListener(
                mListener);

    }

    private void onAddNewAccount() {
        Log.d("refs#2169@@", "Accounts#onAddNewAccount");
        AccountSetupBasics.actionNewAccount(this);
    }

    public void onClick(View view) {
        Log.d("refs#2169@@", "Accounts#onClick");
        onAddNewAccount();
    }
}
