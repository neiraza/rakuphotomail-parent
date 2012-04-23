package jp.co.fttx.rakuphotomail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;

public class AccountSetupOptions extends RakuPhotoActivity {
    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private Account mAccount;



    public static void actionOptions(Context context, Account account, boolean makeDefault) {
        Intent i = new Intent(context, AccountSetupOptions.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        context.startActivity(i);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_options);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        onDone();
    }


    private void onDone() {

        mAccount.setDescription(mAccount.getEmail());
        mAccount.setNotifyNewMail(false);
        mAccount.setShowOngoing(false);
        mAccount.setFolderPushMode(Account.FolderMode.NONE);

        mAccount.save(Preferences.getPreferences(this));
        if (mAccount.equals(Preferences.getPreferences(this).getDefaultAccount()) ||
                getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false)) {
            Preferences.getPreferences(this).setDefaultAccount(mAccount);
        }
        RakuPhotoMail.setServicesEnabled(this);
        AccountSetupNames.actionSetNames(this, mAccount);
        finish();
    }

}
