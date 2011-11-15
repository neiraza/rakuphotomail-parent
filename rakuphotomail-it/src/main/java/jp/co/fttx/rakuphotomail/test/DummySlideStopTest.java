package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.activity.DummySlideStop;
import android.test.ActivityInstrumentationTestCase2;

public class DummySlideStopTest extends ActivityInstrumentationTestCase2<DummySlideStop> {

	private DummySlideStop mActivity;
	
	public DummySlideStopTest() {
		super("jp.co.fttx.rakuphotomail.activity", DummySlideStop.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testHoge(){
		mActivity = getActivity();
		assertNotNull(mActivity);
	}

}
