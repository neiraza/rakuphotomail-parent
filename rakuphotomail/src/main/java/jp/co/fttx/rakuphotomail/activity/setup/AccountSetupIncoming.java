package jp.co.fttx.rakuphotomail.activity.setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import jp.co.fttx.rakuphotomail.*;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;
import jp.co.fttx.rakuphotomail.helper.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class AccountSetupIncoming extends RakuPhotoActivity implements
        OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private static final int popPorts[] = {110, 995, 995, 110, 110};
    private static final int imapPorts[] = {143, 993, 993, 143, 143};
    private static final String imapSchemes[] = {"imap", "imap+ssl",
            "imap+ssl+", "imap+tls", "imap+tls+"};
    private static final String authTypes[] = {"PLAIN", "CRAM_MD5"};

    private int mAccountPorts[];
    private String mAccountSchemes[];
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mServerView;
    private EditText mPortView;
    private Spinner mSecurityTypeView;
    private Spinner mAuthTypeView;
    private Button mNextButton;
    private Account mAccount;
    private boolean mMakeDefault;
    private CheckBox mSubscribedFoldersOnly;

    public static void actionIncomingSettings(Activity context,
                                              Account account, boolean makeDefault) {
        Intent i = new Intent(context, AccountSetupIncoming.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        context.startActivity(i);
    }

    public static void actionEditIncomingSettings(Activity context,
                                                  Account account) {
        Intent i = new Intent(context, AccountSetupIncoming.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("ahokato", "AccountSetupIncoming#onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_incoming);

        mUsernameView = (EditText) findViewById(R.id.account_username);
        mPasswordView = (EditText) findViewById(R.id.account_password);
        TextView serverLabelView = (TextView) findViewById(R.id.account_server_label);
        mServerView = (EditText) findViewById(R.id.account_server);
        mPortView = (EditText) findViewById(R.id.account_port);
        mSecurityTypeView = (Spinner) findViewById(R.id.account_security_type);
        mAuthTypeView = (Spinner) findViewById(R.id.account_auth_type);
        mNextButton = (Button) findViewById(R.id.next);

        mNextButton.setOnClickListener(this);

        SpinnerOption securityTypes[] = {
                new SpinnerOption(
                        0,
                        getString(R.string.account_setup_incoming_security_none_label)),
                new SpinnerOption(
                        1,
                        getString(R.string.account_setup_incoming_security_ssl_optional_label)),
                new SpinnerOption(
                        2,
                        getString(R.string.account_setup_incoming_security_ssl_label)),
                new SpinnerOption(
                        3,
                        getString(R.string.account_setup_incoming_security_tls_optional_label)),
                new SpinnerOption(
                        4,
                        getString(R.string.account_setup_incoming_security_tls_label)),};

        // This needs to be kept in sync with the list at the top of the file.
        // that makes me somewhat unhappy
        SpinnerOption authTypeSpinnerOptions[] = {
                new SpinnerOption(0, "PLAIN"), new SpinnerOption(1, "CRAM_MD5")};

        ArrayAdapter<SpinnerOption> securityTypesAdapter = new ArrayAdapter<SpinnerOption>(
                this, android.R.layout.simple_spinner_item, securityTypes);
        securityTypesAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecurityTypeView.setAdapter(securityTypesAdapter);

        ArrayAdapter<SpinnerOption> authTypesAdapter = new ArrayAdapter<SpinnerOption>(
                this, android.R.layout.simple_spinner_item,
                authTypeSpinnerOptions);
        authTypesAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAuthTypeView.setAdapter(authTypesAdapter);

        /*
           * Updates the port when the user changes the security type. This allows
           * us to show a reasonable default which the user can change.
           */
        mSecurityTypeView
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        updatePortFromSecurityType();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        /*
           * Calls validateFields() which enables or disables the Next button
           * based on the fields' validity.
           */
        TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        };
        mUsernameView.addTextChangedListener(validationTextWatcher);
        mPasswordView.addTextChangedListener(validationTextWatcher);
        mServerView.addTextChangedListener(validationTextWatcher);
        mPortView.addTextChangedListener(validationTextWatcher);

        /*
           * Only allow digits in the port field.
           */
        mPortView.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);

        /*
           * If we're being reloaded we override the original account with the one
           * we saved
           */
        if (savedInstanceState != null
                && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
            mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        }

        try {
            URI uri = new URI(mAccount.getStoreUri());
            String username = null;
            String password = null;
            String authType = null;

            if (uri.getUserInfo() != null) {
                String[] userInfoParts = uri.getUserInfo().split(":");
                if (userInfoParts.length == 3) {
                    authType = userInfoParts[0];
                    username = URLDecoder.decode(userInfoParts[1], "UTF-8");
                    password = URLDecoder.decode(userInfoParts[2], "UTF-8");
                } else if (userInfoParts.length == 2) {
                    username = URLDecoder.decode(userInfoParts[0], "UTF-8");
                    password = URLDecoder.decode(userInfoParts[1], "UTF-8");
                } else if (userInfoParts.length == 1) {
                    username = URLDecoder.decode(userInfoParts[0], "UTF-8");
                }
            }

            if (username != null) {
            mUsernameView.setText(username);
            }

            if (password != null) {
            mPasswordView.setText(password);
            }

            if (authType != null) {
                for (int i = 0; i < authTypes.length; i++) {
                    if (authTypes[i].equals(authType)) {
                        SpinnerOption.setSpinnerOptionValue(mAuthTypeView, i);
                    }
                }
            }
                serverLabelView
                        .setText(R.string.account_setup_incoming_imap_server_label);
                mAccountPorts = imapPorts;
                mAccountSchemes = imapSchemes;
                mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);

            for (int i = 0; i < mAccountSchemes.length; i++) {
                if (mAccountSchemes[i].equals(uri.getScheme())) {
                    SpinnerOption.setSpinnerOptionValue(mSecurityTypeView, i);
                }
            }

			if (uri.getHost() != null) {
				mServerView.setText(uri.getHost());
			}

            if (uri.getPort() != -1) {
                mPortView.setText(Integer.toString(uri.getPort()));
            } else {
                updatePortFromSecurityType();
            }


            validateFields();
        } catch (Exception e) {
            failure(e);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
    }

    private void validateFields() {
        mNextButton.setEnabled(Utility.requiredFieldValid(mUsernameView)
                && Utility.requiredFieldValid(mPasswordView)
                && Utility.domainFieldValid(mServerView)
                && Utility.requiredFieldValid(mPortView));
        Utility.setCompoundDrawablesAlpha(mNextButton,
                mNextButton.isEnabled() ? 255 : 128);
    }

    private void updatePortFromSecurityType() {
        if (mAccountPorts != null) {
            int securityType = (Integer) ((SpinnerOption) mSecurityTypeView
                    .getSelectedItem()).value;
            mPortView.setText(Integer.toString(mAccountPorts[securityType]));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("ahokato", "AccountSetupIncoming#onActivityResult");

        if (resultCode == RESULT_OK) {
            if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                mAccount.save(Preferences.getPreferences(this));
                finish();
            } else {
                try {
                    String usernameEnc = URLEncoder.encode(mUsernameView
                            .getText().toString(), "UTF-8");
                    String passwordEnc = URLEncoder.encode(mPasswordView
                            .getText().toString(), "UTF-8");
                    URI oldUri = new URI(mAccount.getTransportUri());
                    URI uri = new URI(oldUri.getScheme(), usernameEnc + ":"
                            + passwordEnc, oldUri.getHost(), oldUri.getPort(),
                            null, null, null);
                    mAccount.setTransportUri(uri.toString());
                } catch (UnsupportedEncodingException enc) {
                    Log.e(RakuPhotoMail.LOG_TAG,
                            "Couldn't urlencode username or password.", enc);
                } catch (URISyntaxException use) {
                }

                AccountSetupOutgoing.actionOutgoingSettings(this, mAccount,
                        mMakeDefault);
                finish();
            }
        }
    }

    @Override
    protected void onNext() {
        Log.d("ahokato", "AccountSetupIncoming#onNext");

        try {
            int securityType = (Integer) ((SpinnerOption) mSecurityTypeView
                    .getSelectedItem()).value;
            String path = null;
                path = "/";

            final String userInfo;
            String user = mUsernameView.getText().toString();
            String password = mPasswordView.getText().toString();
            String userEnc = URLEncoder.encode(user, "UTF-8");
            String passwordEnc = URLEncoder.encode(password, "UTF-8");

                String authType = ((SpinnerOption) mAuthTypeView
                        .getSelectedItem()).label;
                userInfo = authType + ":" + userEnc + ":" + passwordEnc;
            URI uri = new URI(mAccountSchemes[securityType], userInfo,
                    mServerView.getText().toString(),
                    Integer.parseInt(mPortView.getText().toString()), path, // path
                    null, // query
                    null);
            mAccount.setStoreUri(uri.toString());

            mAccount.setCompression(Account.TYPE_MOBILE,
                    true);
            mAccount.setCompression(Account.TYPE_WIFI,
                    true);
            mAccount.setCompression(Account.TYPE_OTHER,
                    true);

            mAccount.setSubscribedFoldersOnly(false);

            AccountSetupCheckSettings.actionCheckSettings(this, mAccount, true,
                    false);
        } catch (Exception e) {
            failure(e);
        }

    }

    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.next:
                    onNext();
                    break;
            }
        } catch (Exception e) {
            failure(e);
        }
    }

    private void failure(Exception use) {
        Log.e(RakuPhotoMail.LOG_TAG, "Failure", use);
        String toastText = getString(R.string.account_setup_bad_uri,
                use.getMessage());

        Toast toast = Toast.makeText(getApplication(), toastText,
                Toast.LENGTH_LONG);
        toast.show();
    }
}
