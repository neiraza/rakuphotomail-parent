package jp.co.fttx.rakuphotomail.activity;

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
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
	private ImageView mPhoto;
	private Account account;
	private Context mContext;

	// private Button button;

	@Override
	public void onCreate(Bundle icicle) {
		mContext = this;
		super.onCreate(icicle);
		setContentView(R.layout.rakuphoto_top);
		Account[] accounts = Preferences.getPreferences(this).getAccounts();

		// TODO account固定部分の修正が必要
		if (null != accounts && accounts.length > 0) {
			account = accounts[0];
		} else {

		}

		mMail = (ImageView) findViewById(R.id.rakuphoto_mail);
		mPhoto = (ImageView) findViewById(R.id.rakuphoto_photo);

		mMail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(RakuPhotoMail.LOG_TAG, "RakuPhotoMenuSelecter#onCreate:mail open!");
				DummyAccounts.listAccounts(mContext);
				finish();
			}
		});
		mPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(RakuPhotoMail.LOG_TAG, "RakuPhotoMenuSelecter#onCreate SlideShow Open!");
				onSlideShow();
			}
		});
	}
	
	private void onSlideShow() {
		Account realAccount = account;
		GallerySlideShow.actionHandleFolder(this, realAccount, realAccount.getInboxFolderName());
		finish();
	}
}
