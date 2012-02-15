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
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;
import jp.co.fttx.rakuphotomail.helper.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class DummyAccountSetupIncoming extends RakuPhotoActivity implements
		OnClickListener {
	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

	private static final int popPorts[] = { 110, 995, 995, 110, 110 };
	private static final String popSchemes[] = { "pop3", "pop3+ssl",
			"pop3+ssl+", "pop3+tls", "pop3+tls+" };
	private static final int imapPorts[] = { 143, 993, 993, 143, 143 };
	private static final String imapSchemes[] = { "imap", "imap+ssl",
			"imap+ssl+", "imap+tls", "imap+tls+" };
	private static final int webdavPorts[] = { 80, 443, 443, 443, 443 };
	private static final String webdavSchemes[] = { "webdav", "webdav+ssl",
			"webdav+ssl+", "webdav+tls", "webdav+tls+" };

	private static final String authTypes[] = { "PLAIN", "CRAM_MD5" };

	private int mAccountPorts[];
	private String mAccountSchemes[];
	private EditText mUsernameView;
	private EditText mPasswordView;
	private EditText mServerView;
	private EditText mPortView;
	private Spinner mSecurityTypeView;
	private Spinner mAuthTypeView;
	private EditText mImapPathPrefixView;
	private EditText mWebdavPathPrefixView;
	private EditText mWebdavAuthPathView;
	private EditText mWebdavMailboxPathView;
	private Button mNextButton;
	private Account mAccount;
	private boolean mMakeDefault;
	private CheckBox mCompressionMobile;
	private CheckBox mCompressionWifi;
	private CheckBox mCompressionOther;
	private CheckBox mSubscribedFoldersOnly;

	public static void actionIncomingSettings(Activity context,
			Account account, boolean makeDefault) {
        Log.d("refs#2169", "DummyAccountSetupIncoming#actionIncomingSettings start");

		Intent i = new Intent(context, DummyAccountSetupIncoming.class);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
		context.startActivity(i);
        Log.d("refs#2169", "DummyAccountSetupIncoming#actionIncomingSettings end");

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        Log.d("refs#2169", "DummyAccountSetupIncoming#onCreate start");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_incoming);

		mUsernameView = (EditText) findViewById(R.id.account_username);
		mPasswordView = (EditText) findViewById(R.id.account_password);
		TextView serverLabelView = (TextView) findViewById(R.id.account_server_label);
		mServerView = (EditText) findViewById(R.id.account_server);
		mPortView = (EditText) findViewById(R.id.account_port);
		mSecurityTypeView = (Spinner) findViewById(R.id.account_security_type);
		mAuthTypeView = (Spinner) findViewById(R.id.account_auth_type);
		mImapPathPrefixView = (EditText) findViewById(R.id.imap_path_prefix);
		mWebdavPathPrefixView = (EditText) findViewById(R.id.webdav_path_prefix);
		mWebdavAuthPathView = (EditText) findViewById(R.id.webdav_auth_path);
		mWebdavMailboxPathView = (EditText) findViewById(R.id.webdav_mailbox_path);
		mNextButton = (Button) findViewById(R.id.next);
		mCompressionMobile = (CheckBox) findViewById(R.id.compression_mobile);
		mCompressionWifi = (CheckBox) findViewById(R.id.compression_wifi);
		mCompressionOther = (CheckBox) findViewById(R.id.compression_other);
		mSubscribedFoldersOnly = (CheckBox) findViewById(R.id.subscribed_folders_only);

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
						getString(R.string.account_setup_incoming_security_tls_label)), };

		// This needs to be kept in sync with the list at the top of the file.
		// that makes me somewhat unhappy
		SpinnerOption authTypeSpinnerOptions[] = {
				new SpinnerOption(0, "PLAIN"), new SpinnerOption(1, "CRAM_MD5") };

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

			// XXX テスト用
			// if (username != null) {
			// mUsernameView.setText(username);
			// }
            //TODO テスト用アカウント
			mUsernameView.setText("taro.tamachi@rakuphoto.ucom.local");

			// XXX テスト用
			// if (password != null) {
			// mPasswordView.setText(password);
			// }
			mPasswordView.setText("ucomadmin");

			if (authType != null) {
				for (int i = 0; i < authTypes.length; i++) {
					if (authTypes[i].equals(authType)) {
						SpinnerOption.setSpinnerOptionValue(mAuthTypeView, i);
					}
				}
			}

			if (uri.getScheme().startsWith("pop3")) {
				serverLabelView
						.setText(R.string.account_setup_incoming_pop_server_label);
				mAccountPorts = popPorts;
				mAccountSchemes = popSchemes;
				findViewById(R.id.imap_path_prefix_section).setVisibility(
						View.GONE);
				findViewById(R.id.webdav_advanced_header).setVisibility(
						View.GONE);
				findViewById(R.id.webdav_mailbox_alias_section).setVisibility(
						View.GONE);
				findViewById(R.id.webdav_owa_path_section).setVisibility(
						View.GONE);
				findViewById(R.id.webdav_auth_path_section).setVisibility(
						View.GONE);
				findViewById(R.id.compression_section).setVisibility(View.GONE);
				findViewById(R.id.compression_label).setVisibility(View.GONE);
				mSubscribedFoldersOnly.setVisibility(View.GONE);
				mAccount.setDeletePolicy(Account.DELETE_POLICY_NEVER);
			} else if (uri.getScheme().startsWith("imap")) {
				serverLabelView
						.setText(R.string.account_setup_incoming_imap_server_label);
				mAccountPorts = imapPorts;
				mAccountSchemes = imapSchemes;

				if (uri.getPath() != null && uri.getPath().length() > 0) {
					mImapPathPrefixView.setText(uri.getPath().substring(1));
				}

				findViewById(R.id.webdav_advanced_header).setVisibility(
						View.GONE);
				findViewById(R.id.webdav_mailbox_alias_section).setVisibility(
						View.GONE);
				findViewById(R.id.webdav_owa_path_section).setVisibility(
						View.GONE);
				findViewById(R.id.webdav_auth_path_section).setVisibility(
						View.GONE);
				mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);

				if (!Intent.ACTION_EDIT.equals(getIntent().getAction())) {
					findViewById(R.id.imap_folder_setup_section).setVisibility(
							View.GONE);
				}
			} else if (uri.getScheme().startsWith("webdav")) {
				serverLabelView
						.setText(R.string.account_setup_incoming_webdav_server_label);
				mAccountPorts = webdavPorts;
				mAccountSchemes = webdavSchemes;

				/** Hide the unnecessary fields */
				findViewById(R.id.imap_path_prefix_section).setVisibility(
						View.GONE);
				findViewById(R.id.account_auth_type_label).setVisibility(
						View.GONE);
				findViewById(R.id.account_auth_type).setVisibility(View.GONE);
				findViewById(R.id.compression_section).setVisibility(View.GONE);
				findViewById(R.id.compression_label).setVisibility(View.GONE);
				mSubscribedFoldersOnly.setVisibility(View.GONE);
				if (uri.getPath() != null && uri.getPath().length() > 0) {
					String[] pathParts = uri.getPath().split("\\|");

					for (int i = 0, count = pathParts.length; i < count; i++) {
						if (i == 0) {
							if (pathParts[0] != null
									&& pathParts[0].length() > 1) {
								mWebdavPathPrefixView.setText(pathParts[0]
										.substring(1));
							}
						} else if (i == 1) {
							if (pathParts[1] != null
									&& pathParts[1].length() > 1) {
								mWebdavAuthPathView.setText(pathParts[1]);
							}
						} else if (i == 2) {
							if (pathParts[2] != null
									&& pathParts[2].length() > 1) {
								mWebdavMailboxPathView.setText(pathParts[2]);
							}
						}
					}
				}
				mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
			} else {
				throw new Exception("Unknown account type: "
						+ mAccount.getStoreUri());
			}

			for (int i = 0; i < mAccountSchemes.length; i++) {
				if (mAccountSchemes[i].equals(uri.getScheme())) {
					SpinnerOption.setSpinnerOptionValue(mSecurityTypeView, i);
				}
			}
			mCompressionMobile.setChecked(mAccount
					.useCompression(Account.TYPE_MOBILE));
			mCompressionWifi.setChecked(mAccount
					.useCompression(Account.TYPE_WIFI));
			mCompressionOther.setChecked(mAccount
					.useCompression(Account.TYPE_OTHER));

			// XXX テスト用
//			if (uri.getHost() != null) {
//				mServerView.setText(uri.getHost());
//			}
			mServerView.setText("172.29.51.226");

			if (uri.getPort() != -1) {
				mPortView.setText(Integer.toString(uri.getPort()));
			} else {
				updatePortFromSecurityType();
			}

			mSubscribedFoldersOnly.setChecked(mAccount.subscribedFoldersOnly());

			validateFields();
		} catch (Exception e) {
			failure(e);
		}

        Log.d("refs#2169", "DummyAccountSetupIncoming#onCreate end");

	}

    @Override
    public void onResume(){
        Log.d("refs#2169", "DummyAccountSetupIncoming#onResume start");

        super.onResume();

        //TODO ショートカットさん
        next();
        Log.d("refs#2169", "DummyAccountSetupIncoming#onResume end");

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
        Log.d("refs#2169", "DummyAccountSetupIncoming#onActivityResult start");

		if (resultCode == RESULT_OK) {
			if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
				mAccount.save(Preferences.getPreferences(this));
				finish();
			} else {
				/*
				 * Set the username and password for the outgoing settings to
				 * the username and password the user just set for incoming.
				 */
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
					// This really shouldn't happen since the encoding is
					// hardcoded to UTF-8
					Log.e(RakuPhotoMail.LOG_TAG,
							"Couldn't urlencode username or password.", enc);
				} catch (URISyntaxException use) {
					/*
					 * If we can't set up the URL we just continue. It's only
					 * for convenience.
					 */
				}

				DummyAccountSetupOutgoing.actionOutgoingSettings(this, mAccount,
						mMakeDefault);
				finish();
			}
		}
        Log.d("refs#2169", "DummyAccountSetupIncoming#onActivityResult end");

	}

	@Override
	protected void onNext() {
        Log.d("refs#2169", "DummyAccountSetupIncoming#onNext start");
		try {
			int securityType = (Integer) ((SpinnerOption) mSecurityTypeView
					.getSelectedItem()).value;
			String path = null;
			if (mAccountSchemes[securityType].startsWith("imap")) {
				path = "/" + mImapPathPrefixView.getText();
			} else if (mAccountSchemes[securityType].startsWith("webdav")) {
				path = "/" + mWebdavPathPrefixView.getText();
				path = path + "|" + mWebdavAuthPathView.getText();
				path = path + "|" + mWebdavMailboxPathView.getText();
			}

			final String userInfo;
			String user = mUsernameView.getText().toString();
			String password = mPasswordView.getText().toString();
			String userEnc = URLEncoder.encode(user, "UTF-8");
			String passwordEnc = URLEncoder.encode(password, "UTF-8");

			if (mAccountSchemes[securityType].startsWith("imap")) {
				String authType = ((SpinnerOption) mAuthTypeView
						.getSelectedItem()).label;
				userInfo = authType + ":" + userEnc + ":" + passwordEnc;
			} else {
				String authType = ((SpinnerOption) mAuthTypeView
						.getSelectedItem()).label;
				if (!authType.equalsIgnoreCase("plain")) {
					userInfo = authType + ":" + userEnc + ":" + passwordEnc;
				} else {
					userInfo = userEnc + ":" + passwordEnc;
				}
			}
			URI uri = new URI(mAccountSchemes[securityType], userInfo,
					mServerView.getText().toString(),
					Integer.parseInt(mPortView.getText().toString()), path, // path
					null, // query
					null);
			mAccount.setStoreUri(uri.toString());

			mAccount.setCompression(Account.TYPE_MOBILE,
					mCompressionMobile.isChecked());
			mAccount.setCompression(Account.TYPE_WIFI,
					mCompressionWifi.isChecked());
			mAccount.setCompression(Account.TYPE_OTHER,
					mCompressionOther.isChecked());
			mAccount.setSubscribedFoldersOnly(mSubscribedFoldersOnly
					.isChecked());

            DummyAccountSetupCheckSettings.actionCheckSettings(this, mAccount, true,
					false);
		} catch (Exception e) {
			failure(e);
		}
        Log.d("refs#2169", "DummyAccountSetupIncoming#onNext end");
	}

	public void next() {
        Log.d("refs#2169", "DummyAccountSetupIncoming#next start");

		try {
			int securityType = (Integer) ((SpinnerOption) mSecurityTypeView
					.getSelectedItem()).value;
			String path = null;
			if (mAccountSchemes[securityType].startsWith("imap")) {
				path = "/" + mImapPathPrefixView.getText();
			} else if (mAccountSchemes[securityType].startsWith("webdav")) {
				path = "/" + mWebdavPathPrefixView.getText();
				path = path + "|" + mWebdavAuthPathView.getText();
				path = path + "|" + mWebdavMailboxPathView.getText();
			}

			final String userInfo;
			String user = mUsernameView.getText().toString();
			String password = mPasswordView.getText().toString();
			String userEnc = URLEncoder.encode(user, "UTF-8");
			String passwordEnc = URLEncoder.encode(password, "UTF-8");

			if (mAccountSchemes[securityType].startsWith("imap")) {
				String authType = ((SpinnerOption) mAuthTypeView
						.getSelectedItem()).label;
				userInfo = authType + ":" + userEnc + ":" + passwordEnc;
			} else {
				String authType = ((SpinnerOption) mAuthTypeView
						.getSelectedItem()).label;
				if (!authType.equalsIgnoreCase("plain")) {
					userInfo = authType + ":" + userEnc + ":" + passwordEnc;
				} else {
					userInfo = userEnc + ":" + passwordEnc;
				}
			}
			URI uri = new URI(mAccountSchemes[securityType], userInfo,
					mServerView.getText().toString(),
					Integer.parseInt(mPortView.getText().toString()), path, // path
					null, // query
					null);
			mAccount.setStoreUri(uri.toString());

			mAccount.setCompression(Account.TYPE_MOBILE,
					mCompressionMobile.isChecked());
			mAccount.setCompression(Account.TYPE_WIFI,
					mCompressionWifi.isChecked());
			mAccount.setCompression(Account.TYPE_OTHER,
					mCompressionOther.isChecked());
			mAccount.setSubscribedFoldersOnly(mSubscribedFoldersOnly
					.isChecked());

            DummyAccountSetupCheckSettings.actionCheckSettings(this, mAccount, true,
					false);
		} catch (Exception e) {
			failure(e);
		}
        Log.d("refs#2169", "DummyAccountSetupIncoming#next end");

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
