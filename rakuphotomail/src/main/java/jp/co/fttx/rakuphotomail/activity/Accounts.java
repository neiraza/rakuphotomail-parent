package jp.co.fttx.rakuphotomail.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
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
    private ProgressDialog mProgressDialog;

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
        mProgressDialog = new ProgressDialog(mContext);
        Account[] accounts = Preferences.getPreferences(this).getAccounts();
        if (accounts.length == 1 && accounts[0].isAvailable(this)) {
            GallerySlideShow.actionSlideShow(this, accounts[0], accounts[0].getInboxFolderName(), null);
            finish();
        }
        mNext = (Button) findViewById(R.id.next);
        mNext.setOnClickListener(this);
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
        setUpProgressDialog(mProgressDialog, getString(R.string.progress_please_wait), getString(R.string.progress_slideshow_start));
        AccountSetupBasics.actionNewAccount(this);
        mNext.setEnabled(false);
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void onClick(View view) {
        onAddNewAccount();
    }

    /**
     * @param progressDialog progressDialog
     * @param title          title
     * @param message        message
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setUpProgressDialog(ProgressDialog progressDialog, String title, String message) {
        if (!progressDialog.isShowing()) {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }
}
