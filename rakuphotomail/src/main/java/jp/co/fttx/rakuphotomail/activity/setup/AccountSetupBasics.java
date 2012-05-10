package jp.co.fttx.rakuphotomail.activity.setup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import jp.co.fttx.rakuphotomail.*;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;
import jp.co.fttx.rakuphotomail.helper.Utility;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Prompts the user for the email address and password. Also prompts for
 * "Use this account as default" if this is the 2nd+ account being set up.
 * Attempts to lookup default settings for the domain the user specified. If the
 * domain is known the settings are handed off to the AccountSetupCheckSettings
 * activity. If no settings are found the settings are handed off to the
 * AccountSetupAccountType activity.
 */
public class AccountSetupBasics extends RakuPhotoActivity
        implements OnClickListener, TextWatcher, CompoundButton.OnCheckedChangeListener {
    private final static String EXTRA_ACCOUNT = "jp.co.fttx.rakuphotomail.AccountSetupBasics.account";
    private final static int DIALOG_NOTE = 1;
    private final static String STATE_KEY_PROVIDER =
            "jp.co.fttx.rakuphotomail.AccountSetupBasics.provider";

    private Preferences mPrefs;
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mNextButton;
    private Account mAccount;
    private Provider mProvider;
    private CheckBox mPasswordVisibleCheck;
    private int mPasswordVisibleCheckDefaultIType;

    private EmailAddressValidator mEmailValidator = new EmailAddressValidator();

    public static void actionNewAccount(Context context) {
        Intent i = new Intent(context, AccountSetupBasics.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("refs#2853", "AccountSetupBasics#onCrate");
        setContentView(R.layout.account_setup_basics);
        mPrefs = Preferences.getPreferences(this);
        mEmailView = (EditText) findViewById(R.id.account_email);
        mPasswordView = (EditText) findViewById(R.id.account_password);
        mPasswordVisibleCheck = (CheckBox) findViewById(R.id.account_password_visible_checkbox);
        mNextButton = (Button) findViewById(R.id.next);
        mNextButton.setOnClickListener(this);

        mEmailView.addTextChangedListener(this);
        mPasswordView.addTextChangedListener(this);
        mPasswordVisibleCheck.setOnCheckedChangeListener(this);
        mPasswordVisibleCheckDefaultIType = mPasswordView.getInputType();
        mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | mPasswordVisibleCheckDefaultIType);

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            String accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
            mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_KEY_PROVIDER)) {
            mProvider = (Provider) savedInstanceState.getSerializable(STATE_KEY_PROVIDER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("refs#2853", "AccountSetupBasics#onResume");
        validateFields();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAccount != null) {
            outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        }
        if (mProvider != null) {
            outState.putSerializable(STATE_KEY_PROVIDER, mProvider);
        }
    }

    public void afterTextChanged(Editable s) {
        validateFields();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private void validateFields() {
        Log.d("refs#2853", "AccountSetupBasics#validateFields");
        String email = mEmailView.getText().toString();
        Log.d("refs#2853", "AccountSetupBasics#validateFields email:" + email);
        boolean valid = Utility.requiredFieldValid(mEmailView)
                && Utility.requiredFieldValid(mPasswordView)
                && mEmailValidator.isValidAddressOnly(email);
        mNextButton.setEnabled(valid);
    }

    private String getOwnerName() {
        String name = null;
        try {
            name = getDefaultAccountName();
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Could not get default account name", e);
        }

        if (name == null) {
            name = "";
        }
        return name;
    }

    private String getDefaultAccountName() {
        String name = null;
        Account account = Preferences.getPreferences(this).getDefaultAccount();
        if (account != null) {
            name = account.getName();
        }
        return name;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_NOTE) {
            if (mProvider != null && mProvider.note != null) {
                return new AlertDialog.Builder(this)
                        .setMessage(mProvider.note)
                        .setPositiveButton(
                                getString(R.string.okay_action),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finishAutoSetup();
                                    }
                                })
                        .setNegativeButton(
                                getString(R.string.cancel_action),
                                null)
                        .create();
            }
        }
        return null;
    }

    private void finishAutoSetup() {
        Log.d("refs#2853", "AccountSetupBasics#finishAutoSetup");
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String[] emailParts = splitEmail(email);
        String user = emailParts[0];
        String domain = emailParts[1];
        URI incomingUri = null;
        URI outgoingUri = null;
        try {
            String userEnc = URLEncoder.encode(user, "UTF-8");
            String passwordEnc = URLEncoder.encode(password, "UTF-8");

            String incomingUsername = mProvider.incomingUsernameTemplate;
            incomingUsername = incomingUsername.replaceAll("\\$email", email);
            incomingUsername = incomingUsername.replaceAll("\\$user", userEnc);
            incomingUsername = incomingUsername.replaceAll("\\$domain", domain);

            URI incomingUriTemplate = mProvider.incomingUriTemplate;
            incomingUri = new URI(incomingUriTemplate.getScheme(), incomingUsername + ":"
                    + passwordEnc, incomingUriTemplate.getHost(), incomingUriTemplate.getPort(), null,
                    null, null);
            Log.d("refs#2853", "AccountSetupBasics#finishAutoSetup incomingUri:" + incomingUri.toString());

            String outgoingUsername = mProvider.outgoingUsernameTemplate;

            URI outgoingUriTemplate = mProvider.outgoingUriTemplate;


            if (outgoingUsername != null) {
                outgoingUsername = outgoingUsername.replaceAll("\\$email", email);
                outgoingUsername = outgoingUsername.replaceAll("\\$user", userEnc);
                outgoingUsername = outgoingUsername.replaceAll("\\$domain", domain);
                outgoingUri = new URI(outgoingUriTemplate.getScheme(), outgoingUsername + ":"
                        + passwordEnc, outgoingUriTemplate.getHost(), outgoingUriTemplate.getPort(), null,
                        null, null);

            } else {
                outgoingUri = new URI(outgoingUriTemplate.getScheme(),
                        null, outgoingUriTemplate.getHost(), outgoingUriTemplate.getPort(), null,
                        null, null);


            }
            Log.d("refs#2853", "AccountSetupBasics#finishAutoSetup outgoingUri:" + outgoingUri.toString());

            mAccount = Preferences.getPreferences(this).newAccount();
            mAccount.setName(getOwnerName());
            mAccount.setEmail(email);
            mAccount.setStoreUri(incomingUri.toString());
            mAccount.setTransportUri(outgoingUri.toString());
            mAccount.setDraftsFolderName(getString(R.string.special_mailbox_name_drafts));
            mAccount.setTrashFolderName(getString(R.string.special_mailbox_name_trash));
            mAccount.setArchiveFolderName(getString(R.string.special_mailbox_name_archive));
            mAccount.setSpamFolderName(getString(R.string.special_mailbox_name_spam));
            mAccount.setSentFolderName(getString(R.string.special_mailbox_name_sent));
            AccountSetupCheckSettings.actionCheckSettings(this, mAccount, true, true);
        } catch (UnsupportedEncodingException enc) {
            // This really shouldn't happen since the encoding is hardcoded to UTF-8
            Log.e(RakuPhotoMail.LOG_TAG, "Couldn't urlencode username or password.", enc);
        } catch (URISyntaxException use) {
            /*
             * If there is some problem with the URI we give up and go on to
             * manual setup.
             */
            onManualSetup();
        }
    }

    @Override
    protected void onNext() {
        Log.d("refs#2853", "AccountSetupBasics#onNext");
        String email = mEmailView.getText().toString();
        String[] emailParts = splitEmail(email);
        String domain = emailParts[1];
        Log.d("refs#2853", "AccountSetupBasics#onNext email:" + email);
        Log.d("refs#2853", "AccountSetupBasics#onNext emailParts:" + Arrays.toString(emailParts));
        Log.d("refs#2853", "AccountSetupBasics#onNext domain:" + domain);

        mProvider = findProviderForDomain(domain);
        if (mProvider == null) {
            Log.d("refs#2853", "AccountSetupBasics#onNext プロバイダーが不明");
            /*
             * We don't have default settings for this account, start the manual
             * setup process.
             */
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.account_setup_basics_alert_confirmation_title))
                    .setMessage(getString(R.string.account_setup_basics_alert_confirmation_message))
                    .setPositiveButton(
                            getString(R.string.okay_action),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    onManualSetup();
                                }
                            })
                    .setNegativeButton(
                            getString(R.string.cancel_action),null)
                    .show();
            return;
        }

        if (mProvider.note != null) {
            showDialog(DIALOG_NOTE);
        } else {
            finishAutoSetup();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            mAccount.setDescription(mAccount.getEmail());
            mAccount.save(Preferences.getPreferences(this));
            Preferences.getPreferences(this).setDefaultAccount(mAccount);
            RakuPhotoMail.setServicesEnabled(this);
            AccountSetupNames.actionSetNames(this, mAccount);
            finish();
        }

    }

    private void onManualSetup() {
        Log.d("refs#2853", "AccountSetupBasics#onManualSetup");
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String[] emailParts = splitEmail(email);
        String user = emailParts[0];
        String domain = emailParts[1];

        mAccount = Preferences.getPreferences(this).newAccount();
        mAccount.setName(getOwnerName());
        mAccount.setEmail(email);
        try {
            String userEnc = URLEncoder.encode(user, "UTF-8");
            String passwordEnc = URLEncoder.encode(password, "UTF-8");

            URI uri = new URI("placeholder", userEnc + ":" + passwordEnc, "mail." + domain, -1, null,
                    null, null);
            mAccount.setStoreUri(uri.toString());
            mAccount.setTransportUri(uri.toString());
        } catch (UnsupportedEncodingException enc) {
            // This really shouldn't happen since the encoding is hardcoded to UTF-8
            Log.e(RakuPhotoMail.LOG_TAG, "Couldn't urlencode username or password.", enc);
        } catch (URISyntaxException use) {
            /*
             * If we can't set up the URL we just continue. It's only for
             * convenience.
             */
        }
        mAccount.setDraftsFolderName(getString(R.string.special_mailbox_name_drafts));
        mAccount.setTrashFolderName(getString(R.string.special_mailbox_name_trash));
        mAccount.setSentFolderName(getString(R.string.special_mailbox_name_sent));

        AccountSetupAccountType.actionSelectAccountType(this, mAccount, true);
        finish();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                onNext();
                break;
        }
    }

    /**
     * Attempts to get the given attribute as a String resource first, and if it fails
     * returns the attribute as a simple String value.
     *
     * @param xml
     * @param name
     * @return
     */
    private String getXmlAttribute(XmlResourceParser xml, String name) {

        int resId = xml.getAttributeResourceValue(null, name, 0);
        if (resId == 0) {
            return xml.getAttributeValue(null, name);
        } else {
            return getString(resId);
        }
    }

    private Provider findProviderForDomain(String domain) {
        Log.d("refs#2853", "AccountSetupBasics#findProviderForDomain domain:" + domain);
        try {
            XmlResourceParser xml = getResources().getXml(R.xml.providers);
            int xmlEventType;
            Provider provider = null;
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG
                        && "provider".equals(xml.getName())
                        && domain.equalsIgnoreCase(getXmlAttribute(xml, "domain"))) {
                    Log.d("refs#2853", "AccountSetupBasics#findProviderForDomain 1");
                    provider = new Provider();
                    provider.id = getXmlAttribute(xml, "id");
                    provider.label = getXmlAttribute(xml, "label");
                    provider.domain = getXmlAttribute(xml, "domain");
                    provider.note = getXmlAttribute(xml, "note");
                } else if (xmlEventType == XmlResourceParser.START_TAG
                        && "incoming".equals(xml.getName())
                        && provider != null) {
                    Log.d("refs#2853", "AccountSetupBasics#findProviderForDomain 2");
                    provider.incomingUriTemplate = new URI(getXmlAttribute(xml, "uri"));
                    provider.incomingUsernameTemplate = getXmlAttribute(xml, "username");
                } else if (xmlEventType == XmlResourceParser.START_TAG
                        && "outgoing".equals(xml.getName())
                        && provider != null) {
                    Log.d("refs#2853", "AccountSetupBasics#findProviderForDomain 3");
                    provider.outgoingUriTemplate = new URI(getXmlAttribute(xml, "uri"));
                    provider.outgoingUsernameTemplate = getXmlAttribute(xml, "username");
                } else if (xmlEventType == XmlResourceParser.END_TAG
                        && "provider".equals(xml.getName())
                        && provider != null) {
                    Log.d("refs#2853", "AccountSetupBasics#findProviderForDomain 4");
                    return provider;
                }
            }
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Error while trying to load provider settings.", e);
        }
        return null;
    }

    private String[] splitEmail(String email) {

        String[] retParts = new String[2];
        String[] emailParts = email.split("@");
        retParts[0] = (emailParts.length > 0) ? emailParts[0] : "";
        retParts[1] = (emailParts.length > 1) ? emailParts[1] : "";
        return retParts;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (mPasswordVisibleCheck.getId()) {
            case R.id.account_password_visible_checkbox:
                if (mPasswordVisibleCheck.isChecked()) {
                    mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | mPasswordVisibleCheckDefaultIType);
                    mPasswordVisibleCheck.setText(getString(R.string.account_password_visible_checkbox_off));
                } else {
                    mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | mPasswordVisibleCheckDefaultIType);
                    mPasswordVisibleCheck.setText(getString(R.string.account_password_visible_checkbox_on));
                }
                mPasswordView.setSelection(mPasswordView.getText().length());
                break;
            default:
        }
    }

    static class Provider implements Serializable {
        private static final long serialVersionUID = 8511656164616538989L;

        public String id;

        public String label;

        public String domain;

        public URI incomingUriTemplate;

        public String incomingUsernameTemplate;

        public URI outgoingUriTemplate;

        public String outgoingUsernameTemplate;

        public String note;
    }
}
