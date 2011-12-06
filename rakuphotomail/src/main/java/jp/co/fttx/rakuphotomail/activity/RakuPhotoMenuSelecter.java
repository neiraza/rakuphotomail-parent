package jp.co.fttx.rakuphotomail.activity;

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class RakuPhotoMenuSelecter extends Activity {
	private ImageView mMail;
	private ImageView mOther;
	private Context mContext;
	private Account account;

	// private Button button;

	@Override
	public void onCreate(Bundle icicle) {
		mContext = this;
		super.onCreate(icicle);
		setContentView(R.layout.rakuphoto_top);

		mMail = (ImageView) findViewById(R.id.rakuphoto_mail);

		mMail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DummyAccounts.listAccounts(mContext);
			}
		});

		mOther = (ImageView) findViewById(R.id.rakuphoto_other);
		mOther.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOther();
			}
		});
	}

	private void onOther() {
		Account[] accounts = Preferences.getPreferences(this).getAccounts();
		if (null != accounts && accounts.length > 0) {
			account = accounts[0];
			System.out.println(account);
		}
	}
}
