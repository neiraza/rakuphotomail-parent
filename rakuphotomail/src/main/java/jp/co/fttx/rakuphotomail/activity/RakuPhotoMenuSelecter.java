package jp.co.fttx.rakuphotomail.activity;

import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class RakuPhotoMenuSelecter extends Activity {
	private ImageView mMail;
	private Context mContext;

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
				Log.v(RakuPhotoMail.LOG_TAG,
						"RakuPhotoMenuSelecter#onCreate:mail open!");
				DummyAccounts.listAccounts(mContext);
				finish();
			}
		});
	}
}
