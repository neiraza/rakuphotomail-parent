package jp.co.fttx.rakuphotomail.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import jp.co.fttx.rakuphotomail.*;
import jp.co.fttx.rakuphotomail.activity.setup.AccountSetupBasics;
import jp.co.fttx.rakuphotomail.controller.MessagingController;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;


public class Accounts extends RakuPhotoActivity implements OnClickListener {

    private Context mContext;
    private Button mNext;
    private ProgressDialog mDialog;

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
            Log.d("ahokato", "Accounts#synchronizeMailboxFinished");
            GallerySlideShow.actionSlideShow(mContext, accounts[0], accounts[0].getInboxFolderName(), null);
            finish();
//            startActivity(new Intent(mContext, Accounts.class));
//            finish();
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
        Log.d("ahokato", "Accounts#onCreate");

        super.onCreate(icicle);
        mContext = this;
        setContentView(R.layout.accounts);
        Account[] accounts = Preferences.getPreferences(this).getAccounts();
        if (accounts.length == 1 && accounts[0].isAvailable(this)) {
            Log.d("ahokato", "Accounts#onCreate GallerySlideShow start!");
            GallerySlideShow.actionSlideShow(this, accounts[0], accounts[0].getInboxFolderName(), null);
            finish();
        }
         mNext = (Button)findViewById(R.id.next);
        mNext.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.d("ahokato", "Accounts#onResume");

        super.onResume();
        MessagingController.getInstance(getApplication())
                .addListener(mListener);
        MessageSync.addListener(mListener);
    }

    @Override
    public void onPause() {
        Log.d("ahokato", "Accounts#onPause");

        super.onPause();
        MessagingController.getInstance(getApplication()).removeListener(
                mListener);
//        MessageSync.removeListener(mListener);
    }

    private void onAddNewAccount() {
        AccountSetupBasics.actionNewAccount(this);
        mNext.setEnabled(false);
        mDialog = new ProgressDialog(mContext);
        mDialog.setTitle("Please wait");
        mDialog.setMessage("現在、設定を読み込み中です。");
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(true);
        mDialog.setMax(100);
        mDialog.setProgress(0);
        mDialog.show();
    }

    public void onClick(View view) {
        onAddNewAccount();
    }
}
