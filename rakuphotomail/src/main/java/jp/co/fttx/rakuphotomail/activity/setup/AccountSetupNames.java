package jp.co.fttx.rakuphotomail.activity.setup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
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
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;

public class AccountSetupNames extends RakuPhotoActivity implements OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";

    private EditText mName;

    private Account mAccount;

    private Button mDoneButton;

    private Spinner mSlideSleepTimeDuration;
    private Spinner mServerSyncTimeDuration;
    private Spinner mScaleRatio;

    private String[] slideSleepTimeDuration;
    private String[] serverSyncTimeDuration;
    private String[] ratioValues;

    private CheckBox mSleepMode;

    private ProgressDialog mProgressDialog;

    public static void actionSetNames(Context context, Account account) {
        Intent i = new Intent(context, AccountSetupNames.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_names);
        mName = (EditText) findViewById(R.id.account_name);
        mDoneButton = (Button) findViewById(R.id.done);
        mDoneButton.setOnClickListener(this);
        mProgressDialog = new ProgressDialog(this);

        TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        mName.addTextChangedListener(validationTextWatcher);

        mName.setKeyListener(TextKeyListener.getInstance(false, Capitalize.WORDS));

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        /*
         * Since this field is considered optional, we don't set this here. If
         * the user fills in a value we'll reset the current value, otherwise we
         * just leave the saved value alone.
         */
        if (mAccount.getName() != null) {
            mName.setText(mAccount.getName());
        }
        if (!Utility.requiredFieldValid(mName)) {
            mDoneButton.setEnabled(false);
        }

        /* Slide SleepTime Duration */
        slideSleepTimeDuration = getResources().getStringArray(R.array.account_settings_slide_change_duration_values);
        mSlideSleepTimeDuration = (Spinner) findViewById(R.id.slideSleepTimeDuration);
        mSlideSleepTimeDuration.setSelection(2);
        mSlideSleepTimeDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAccount.setSlideSleepTime(Long.parseLong(slideSleepTimeDuration[position]));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* Server SyncTime Duration */
        serverSyncTimeDuration = getResources().getStringArray(R.array.account_settings_server_sync_values);
        mServerSyncTimeDuration = (Spinner) findViewById(R.id.account_option_server_sync);
        mServerSyncTimeDuration.setSelection(2);
        mServerSyncTimeDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAccount.setServerSyncTimeDuration(Long.parseLong(serverSyncTimeDuration[position]));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* Scale Ratio */
        ratioValues = getResources().getStringArray(R.array.account_settings_scale_ratio_duration_values);
        mScaleRatio = (Spinner) findViewById(R.id.account_option_scale_ratio);
        mScaleRatio.setSelection(0);
        mScaleRatio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAccount.setScaleRatio(Integer.parseInt(ratioValues[position]));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* Sleep Mode */
        Log.d("flying", "AccountSetupNames#onCreate");
        mSleepMode = (CheckBox) findViewById(R.id.account_option_sleep_mode);
        mSleepMode.setText(getString(R.string.account_settings_slide_show_sleep_mode_summary_off));
        mSleepMode.setOnClickListener(this);
    }

    private void validateFields() {
        mDoneButton.setEnabled(Utility.requiredFieldValid(mName));
        Utility.setCompoundDrawablesAlpha(mDoneButton, mDoneButton.isEnabled() ? 255 : 128);
    }

    @Override
    protected void onNext() {
        setUpProgressDialog(mProgressDialog, "TEST", "TEST");
        mAccount.setDescription(mAccount.getDescription());
        mAccount.setName(mName.getText().toString());
        mAccount.save(Preferences.getPreferences(this));
        try {
            MessageSync.synchronizeMailboxFinished(mAccount, mAccount.getInboxFolderName());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        }
        dismissProgressDialog(mProgressDialog);
        finish();
    }

    private void setSleep() {
        Log.d("flying", "AccountSetupNames#setSleep");
        if (mSleepMode.isChecked()) {
            mSleepMode.setText(getString(R.string.account_settings_slide_show_sleep_mode_summary_on));
            mAccount.setCanSleep(false);
        } else {
            mSleepMode.setText(getString(R.string.account_settings_slide_show_sleep_mode_summary_off));
            mAccount.setCanSleep(true);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done:
                onNext();
                break;
            case R.id.account_option_sleep_mode:
                Log.d("flying", "AccountSetupNames#onClick");
                setSleep();
                break;
            default:
                break;
        }
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

    /**
     * @param progressDialog progressDialog
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void dismissProgressDialog(ProgressDialog progressDialog) {
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
