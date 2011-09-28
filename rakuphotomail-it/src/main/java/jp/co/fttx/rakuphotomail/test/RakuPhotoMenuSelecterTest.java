package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.activity.RakuPhotoMenuSelecter;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

public class RakuPhotoMenuSelecterTest extends
		ActivityInstrumentationTestCase2<RakuPhotoMenuSelecter> {
	public RakuPhotoMenuSelecterTest() {
		super("jp.co.fttx.rakuphotomail", RakuPhotoMenuSelecter.class);
	}
	@Override
	protected void setUp() throws Exception{
		setActivityInitialTouchMode(false);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		setActivityIntent(intent);
	}
	
	public void testHogeee() {
		assertNotNull(this.getActivity());
	}
}
