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
        Log.d("maguro", "DummyAcounts#onCreate start");
        super.onCreate(icicle);
        setContentView(R.layout.dummy_accounts);
        setUp();
        Account[] accounts = Preferences.getPreferences(this).getAccounts();
        if (accounts.length == 1 && accounts[0].isAvailable(this)) {
            Log.d("maguro", "DummyAcounts#onCreate 1");
            GallerySlideShow.actionHandleFolder(this, accounts[0], accounts[0].getInboxFolderName());
            finish();
        } else {
            Log.d("maguro", "DummyAcounts#onCreate 2");
            onAddNewAccount();
        }
        Log.d("maguro", "DummyAcounts#onCreate end");
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
        Log.d("maguro", "DummyAcounts#onResume start");

        super.onResume();
        MessagingController.getInstance(getApplication())
                .addListener(mListener);
        Log.d("maguro", "DummyAcounts#onResume end");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("maguro", "DummyAcounts#onSaveInstanceState start");
        super.onSaveInstanceState(outState);
        Log.d("maguro", "DummyAcounts#onSaveInstanceState end");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("maguro", "DummyAcounts#onRestoreInstanceState start");
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("maguro", "DummyAcounts#onRestoreInstanceState end");
    }


    private void onAddNewAccount() {
        Log.d("maguro", "DummyAccounts#onAddNewAccount start");
        DummyAccountSetupBasics.actionNewAccount(this);
        Log.d("maguro", "DummyAccounts#onAddNewAccount end");
    }

    ActivityListener mListener = new ActivityListener() {

        // TODO
        @Override
        public void folderStatusChanged(Account account, String folderName,
                                        int unreadMessageCount) {
            Log.d("maguro", "ActivityListener mListener#folderStatusChanged");
        }

        @Override
        public void accountStatusChanged(BaseAccount account, AccountStats stats) {
            Log.d("maguro", "ActivityListener mListener#accountStatusChanged");
        }

        @Override
        public void accountSizeChanged(Account account, long oldSize,
                                       long newSize) {
            Log.d("maguro", "ActivityListener mListener#accountSizeChanged");
        }


        // TODO
        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            super.synchronizeMailboxStarted(account, folder);
            Log.d("maguro", "ActivityListener mListener#synchronizeMailboxStarted");
        }

        @Override
        public void synchronizeMailboxFailed(Account account, String folder,
                                             String message) {
            super.synchronizeMailboxFailed(account, folder, message);
            Log.d("maguro", "ActivityListener mListener#synchronizeMailboxFailed");
        }

        // TODO
        @Override
        public void synchronizeMailboxFinished(Account account, String folder,
                                               int totalMessagesInMailbox, int numNewMessages) {
            Log.d("maguro", "ActivityListener mListener#synchronizeMailboxFinished start");
            startActivity(new Intent(mContext, DummyAccounts.class));
            finish();
            Log.d("maguro", "ActivityListener mListener#synchronizeMailboxFinished end");
        }
    };
}
