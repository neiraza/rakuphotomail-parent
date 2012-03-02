package jp.co.fttx.rakuphotomail.activity.setup;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

    private Spinner mAttachmentCacheLimitCount;
    private Spinner mSlideSleepTimeDuration;
    private Spinner mServerSyncTimeDuration;
    private Spinner mScaleRatio;
    private Spinner mDownloadSize;

    private String[] attachmentCacheLimitCount;
    private String[] slideSleepTimeDuration;
    private String[] serverSyncTimeDuration;
    private String[] ratioValues;
    private String[] downloadSize;

    public static void actionSetNames(Context context, Account account) {
        Intent i = new Intent(context, AccountSetupNames.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("ahokato", "AccountSetupNames#onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_names);
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

        /* Attachment Cache Limit Count */
        attachmentCacheLimitCount = getResources().getStringArray(R.array.account_settings_download_cache_values);
        mAttachmentCacheLimitCount = (Spinner) findViewById(R.id.account_option_download_cache);
        mAttachmentCacheLimitCount.setSelection(1);
        mAttachmentCacheLimitCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAccount.setAttachmentCacheLimitCount(Integer.parseInt(attachmentCacheLimitCount[position]));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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


        /* Donwload Size / mail  */
        downloadSize = getResources().getStringArray(R.array.account_settings_download_message_size_values);
        mDownloadSize = (Spinner) findViewById(R.id.account_option_download_message_size);
        mDownloadSize.setSelection(3);
        mDownloadSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAccount.setMaximumAutoDownloadMessageSize(Integer.parseInt(downloadSize[position]));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void validateFields() {
        mDoneButton.setEnabled(Utility.requiredFieldValid(mName));
        Utility.setCompoundDrawablesAlpha(mDoneButton, mDoneButton.isEnabled() ? 255 : 128);
    }

    @Override
    protected void onNext() {
        Log.d("ahokato", "AccountSetupNames#onNext");

        mAccount.setDescription(mAccount.getDescription());
        mAccount.setName(mName.getText().toString());
        mAccount.save(Preferences.getPreferences(this));
        try {
            MessageSync.synchronizeMailboxFinished(mAccount, mAccount.getInboxFolderName());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, getString(R.string.error_messaging_exception) + e.getMessage());
        }
        finish();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done:
                onNext();
                break;
        }
    }
}
