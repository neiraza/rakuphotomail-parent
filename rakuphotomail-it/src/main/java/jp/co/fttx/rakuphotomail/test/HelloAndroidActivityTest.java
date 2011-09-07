package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.HelloAndroidActivity;
import android.test.ActivityInstrumentationTestCase2;

public class HelloAndroidActivityTest extends
		ActivityInstrumentationTestCase2<HelloAndroidActivity> {

	public HelloAndroidActivityTest() {
		super(HelloAndroidActivity.class);
	}

	public void testFuga() {
		assertNotNull(getActivity());
	}
}
