/**
 * 
 */
package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.service.AttachmentSynqService;
import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author tooru.oguri
 * @param <AttachmentSynqService>
 * 
 */
public class AttachmentSynqServiceTest extends
		ServiceTestCase<AttachmentSynqService> {

	AttachmentSynqService testService;

	public AttachmentSynqServiceTest() {
		super(AttachmentSynqService.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {

	}

	@SmallTest
	public void testHoge() {
		IBinder binder = bindService(new Intent());
		testService = ((AttachmentSynqService.AttachmentSynqBinder) binder)
				.getService();
	}
}
