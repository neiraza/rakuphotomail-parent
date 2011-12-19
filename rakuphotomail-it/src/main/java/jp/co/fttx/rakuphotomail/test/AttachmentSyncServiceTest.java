/**
 * 
 */
package jp.co.fttx.rakuphotomail.test;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import jp.co.fttx.rakuphotomail.service.AttachmentSyncService;

/**
 * @author tooru.oguri
 * @param <AttachmentSyncService>
 * 
 */
public class AttachmentSyncServiceTest extends
		ServiceTestCase<AttachmentSyncService> {

    AttachmentSyncService testService;

	public AttachmentSyncServiceTest() {
		super(AttachmentSyncService.class);
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
		testService = ((AttachmentSyncService.AttachmentSyncBinder) binder)
				.getService();
	}
}
