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
import android.widget.Button;
import android.widget.EditText;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;
import jp.co.fttx.rakuphotomail.helper.Utility;

public class DummyAccountSetupNames extends RakuPhotoActivity implements OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";

    private EditText mDescription;

    private EditText mName;

    private Account mAccount;

    private Button mDoneButton;

    public static void actionSetNames(Context context, Account account) {
        Log.d("redbull", "DummyAccountSetupNames#actionSetNames start");

        Intent i = new Intent(context, DummyAccountSetupNames.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
        Log.d("redbull", "DummyAccountSetupNames#actionSetNames end");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("redbull", "DummyAccountSetupNames#onCreate start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_names);
        mDescription = (EditText)findViewById(R.id.account_description);
        mName = (EditText)findViewById(R.id.account_name);
        mDoneButton = (Button)findViewById(R.id.done);
        mDoneButton.setOnClickListener(this);


        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mAccount.setDescription("おぐりさん");
        mAccount.setName("おぐりん");

        Log.d("redbull", "DummyAccountSetupNames#onCreate end");
    }

    @Override
    public void onResume(){
        super.onResume();
        next();
    }

    protected void next() {
        Log.d("redbull", "DummyAccountSetupNames#next start");
        mAccount.save(Preferences.getPreferences(this));
        finish();
        Log.d("redbull", "DummyAccountSetupNames#next end");
    }

    @Override
    protected void onNext() {
        Log.d("redbull", "DummyAccountSetupNames#onNext start");
        mAccount.save(Preferences.getPreferences(this));
        finish();
        Log.d("redbull", "DummyAccountSetupNames#onNext end");

    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.done:
            onNext();
            break;
        }
    }
}
