package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.HelloAndroidActivity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class HelloAndroidActivityTest2 extends
		ActivityUnitTestCase<HelloAndroidActivity> {
	
	public HelloAndroidActivityTest2(){
		super(HelloAndroidActivity.class);
	}

	public void testFuga() {
		startActivity(new Intent(),null,null);
		final HelloAndroidActivity activity = getActivity();
		
		assertNotNull(activity);
	}
}
