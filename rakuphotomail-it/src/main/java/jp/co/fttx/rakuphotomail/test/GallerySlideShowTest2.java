package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.GallerySlideShow;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.util.Log;

public class GallerySlideShowTest2 extends
		ActivityUnitTestCase<GallerySlideShow> {

	public GallerySlideShowTest2() {
		super(GallerySlideShow.class);
	}

	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_FOLDER = "folder";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testHoge() {
		Log.d("steinsgate", "1");
		Context mockContext = new MockRakuContext(getInstrumentation().getTargetContext());
		Log.d("steinsgate", "2");
//		MockRakuApplication mockApp = new MockRakuApplication(mockContext);
//		RakuPhotoMail raku = new RakuPhotoMail();
//		raku.onCreate();
//		setApplication(RakuPhotoMail.app);
		Log.d("steinsgate", "2.1");
		setActivityContext(mockContext);
		Log.d("steinsgate", "2.2");
		Preferences pre = Preferences.getPreferences(mockContext);
		Log.d("steinsgate", "2.5");
		Account account = pre.newAccount();
		Log.d("steinsgate", "3");
		pre.setDefaultAccount(account);
		Log.d("steinsgate", "4");
		Intent intent = new Intent();
		Log.d("steinsgate", "5");
		intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
		Log.d("steinsgate", "6");
		intent.putExtra(EXTRA_FOLDER, account.getInboxFolderName());
		Log.d("steinsgate", "7");
		startActivity(intent, null, null);
		Log.d("steinsgate", "8");
		
		assertNotNull(getActivity());
		Log.d("steinsgate", "geeeeeeeeeeeeeeeeeeeeeeeeeeee");
	}

}
