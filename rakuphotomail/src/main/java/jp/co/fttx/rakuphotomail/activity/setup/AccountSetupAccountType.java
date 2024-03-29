
package jp.co.fttx.rakuphotomail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;

import java.net.URI;

/**
 * Prompts the user to select an account type. The account type, along with the
 * passed in email address, password and makeDefault are then passed on to the
 * AccountSetupIncoming activity.
 */
public class AccountSetupAccountType extends RakuPhotoActivity {
    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private Account mAccount;

    private boolean mMakeDefault;

    public static void actionSelectAccountType(Context context, Account account, boolean makeDefault) {
        Intent i = new Intent(context, AccountSetupAccountType.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
        onImap();
    }

    private void onImap() {
        try {
            URI uri = new URI(mAccount.getStoreUri());
            uri = new URI("imap", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            mAccount.setStoreUri(uri.toString());
            AccountSetupIncoming.actionIncomingSettings(this, mAccount, mMakeDefault);
            finish();
        } catch (Exception use) {
            failure(use);
        }

    }

    private void failure(Exception use) {
        Log.e(RakuPhotoMail.LOG_TAG, "Failure", use);
        String toastText = getString(R.string.account_setup_bad_uri, use.getMessage());

        Toast toast = Toast.makeText(getApplication(), toastText, Toast.LENGTH_LONG);
        toast.show();
    }
}
