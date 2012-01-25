package jp.co.fttx.rakuphotomail.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.*;
import jp.co.fttx.rakuphotomail.activity.setup.DummyAccountSetupBasics;
import jp.co.fttx.rakuphotomail.controller.MessagingController;

public class DummyAccounts extends RakuPhotoActivity {

    private Context mContext;
    private TextView mMessage;

    @Override
    public void onCreate(Bundle icicle) {
        Log.d("refs#2169", "DummyAccounts#onCreate start");
        super.onCreate(icicle);
        setContentView(R.layout.dummy_accounts);
        setUp();
        Account[] accounts = Preferences.getPreferences(this).getAccounts();
        if (accounts.length == 1 && accounts[0].isAvailable(this)) {
            Log.d("refs#2169", "DummyAccounts#onCreate アカウントあるからスライドいくぜー");
            GallerySlideShow.actionSlideShow(this, accounts[0], accounts[0].getInboxFolderName(), null);
            finish();
        } else {
            Log.d("refs#2169", "DummyAccounts#onCreate アカウントつくるぜー");
            onAddNewAccount();
        }
        Log.d("refs#2169", "DummyAccounts#onCreate end");
    }

    private void setUp() {
        mContext = this;
        mMessage = (TextView) findViewById(R.id.dummy_message);
        mMessage.setText("ちょっと、待ってくれや");
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.dummy_progress);
        progressBar.setMax(100);
        progressBar.setProgress(50);
        progressBar.setSecondaryProgress(75);
        progressBar.setIndeterminate(true);
    }

    @Override
    public void onResume() {
        Log.d("refs#2169", "DummyAccounts#onResume start");

        super.onResume();
        MessagingController.getInstance(getApplication())
                .addListener(mListener);
        Log.d("refs#2169", "DummyAccounts#onResume end");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("refs#2169", "DummyAccounts#onSaveInstanceState start");
        super.onSaveInstanceState(outState);
        Log.d("refs#2169", "DummyAccounts#onSaveInstanceState end");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("refs#2169", "DummyAccounts#onRestoreInstanceState start");
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("refs#2169", "DummyAccounts#onRestoreInstanceState end");
    }


    private void onAddNewAccount() {
        Log.d("refs#2169", "DummyAccounts#onAddNewAccount start");
        DummyAccountSetupBasics.actionNewAccount(this);
        Log.d("refs#2169", "DummyAccounts#onAddNewAccount end");
    }

    ActivityListener mListener = new ActivityListener() {

        @Override
        public void folderStatusChanged(Account account, String folderName,
                                        int unreadMessageCount) {
            Log.d("refs#2169", "DummyAccounts ActivityListener mListener#folderStatusChanged");
        }

        @Override
        public void accountStatusChanged(BaseAccount account, AccountStats stats) {
            Log.d("refs#2169", "DummyAccounts ActivityListener mListener#accountStatusChanged");
        }

        @Override
        public void accountSizeChanged(Account account, long oldSize,
                                       long newSize) {
            Log.d("refs#2169", "DummyAccounts ActivityListener mListener#accountSizeChanged");
        }


        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            super.synchronizeMailboxStarted(account, folder);
            Log.d("refs#2169", "DummyAccounts ActivityListener mListener#synchronizeMailboxStarted");
        }

        @Override
        public void synchronizeMailboxFailed(Account account, String folder,
                                             String message) {
            super.synchronizeMailboxFailed(account, folder, message);
            Log.d("refs#2169", "DummyAccounts ActivityListener mListener#synchronizeMailboxFailed");
        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder,
                                               int totalMessagesInMailbox, int numNewMessages) {
            Log.d("refs#2169", "DummyAccounts ActivityListener mListener#synchronizeMailboxFinished start");
            startActivity(new Intent(mContext, DummyAccounts.class));
            finish();
            Log.d("refs#2169", "DummyAccounts ActivityListener mListener#synchronizeMailboxFinished end");
        }
    };
}
