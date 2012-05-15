package jp.co.fttx.rakuphotomail.activity.setup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
    private Context mContext;

    private Button mDoneButton;

    private Spinner mSlideSleepTimeDuration;
    private Spinner mServerSyncTimeDuration;
    private Spinner mScaleRatio;

    private String[] slideSleepTimeDuration;
    private String[] serverSyncTimeDuration;
    private String[] ratioValues;

    private CheckBox mSleepMode;
    private CheckBox mSlideInfo;

    public static void actionSetNames(Context context, Account account) {
        Intent i = new Intent(context, AccountSetupNames.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_names);
        mContext = this;
        mName = (EditText) findViewById(R.id.account_name);
        mDoneButton = (Button) findViewById(R.id.done);
        mDoneButton.setOnClickListener(this);

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
        mAccount = Preferences.getPreferences(mContext).getAccount(accountUuid);

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
        mSleepMode = (CheckBox) findViewById(R.id.account_option_sleep_mode);
        mSleepMode.setText(getString(R.string.account_settings_slide_show_sleep_mode_summary_on));
        mSleepMode.setOnClickListener(this);

        /* slideshow info */
        mSlideInfo = (CheckBox) findViewById(R.id.account_option_slideshow_info_disp);
        mSlideInfo.setText(getString(R.string.account_settings_slide_show_info_disp_summary_on));
        mSlideInfo.setOnClickListener(this);
    }

    private void validateFields() {
        mDoneButton.setEnabled(Utility.requiredFieldValid(mName));
    }

    @Override
    protected void onNext() {
        new FinishedSetup(mContext).execute();
    }

    private void setSleep() {
        if (mSleepMode.isChecked()) {
            mSleepMode.setText(getString(R.string.account_settings_slide_show_sleep_mode_summary_on));
            mAccount.setCanSleep(true);
        } else {
            mSleepMode.setText(getString(R.string.account_settings_slide_show_sleep_mode_summary_off));
            mAccount.setCanSleep(false);
        }
    }

    private void setSlideshowInfo() {
        if (mSlideInfo.isChecked()) {
            mSlideInfo.setText(getString(R.string.account_settings_slide_show_info_disp_summary_on));
            mAccount.setCanDispSlideShowInfo(true);
        } else {
            mSlideInfo.setText(getString(R.string.account_settings_slide_show_info_disp_summary_off));
            mAccount.setCanDispSlideShowInfo(false);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done:
                onNext();
                break;
            case R.id.account_option_sleep_mode:
                setSleep();
                break;
            case R.id.account_option_slideshow_info_disp:
                setSlideshowInfo();
                break;
            default:
                break;
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class FinishedSetup extends AsyncTask<Void, Void, Void> implements DialogInterface.OnCancelListener {
        Context context;
        ProgressDialog progressDialog;

        public FinishedSetup(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(getString(R.string.progress_please_wait));
            progressDialog.setMessage("設定を保存中です\nしばらくお待ちください");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(this);
            progressDialog.show();
        }

        /**
         * @return null
         */
        @Override
        protected Void doInBackground(Void... params) {
            mAccount.setDescription(mAccount.getDescription());
            mAccount.setName(mName.getText().toString());
            mAccount.save(Preferences.getPreferences(context));
            try {
                MessageSync.synchronizeMailboxFinished(mAccount, mAccount.getInboxFolderName());
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
        }

        @Override
        protected void onCancelled() {
            if (null != progressDialog && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(Void tmp) {
            onCancelled();
            finish();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }

}
