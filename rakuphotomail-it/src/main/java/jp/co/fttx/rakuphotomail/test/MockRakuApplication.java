package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import android.content.Context;
import android.test.mock.MockApplication;


public class MockRakuApplication extends MockApplication {

	@Override
	public String getPackageName(){
		return "jp.co.fttx.rakuphotomail";
	}
	
	@Override
	public Context getApplicationContext() {
		return RakuPhotoMail.app;
	}
	
}
