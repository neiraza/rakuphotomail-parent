/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.activity.GallerySendingMail;
import jp.co.fttx.rakuphotomail.activity.MessageReference;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GallerySendingMailTest.
 * 
 * @author tooru.oguri
 * 
 */
public class GallerySendingMailTest extends
		ActivityInstrumentationTestCase2<GallerySendingMail> {

	private GallerySendingMail mActivity;
	private EditText mMailContent;
	private TextView mTo;
	private TextView mToName;
	 private TextView mSnetFlag;
	private Button mSend;
	private static final String EXTRA_ADDRESS_FROM = "addressFrom";
	private static final String EXTRA_ADDRESS_FROM_NAME = "addressFromName";
	private static final String EXTRA_ADDRESS_TO = "addressTo";
	private static final String EXTRA_ADDRESS_TO_NAME = "addressToName";
	private static final String EXTRA_ADDRESS_REPLY_TO = "addressReplyTo";
	private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
	MessageReference mReference;
	MessageBean mMessageBean;

	public GallerySendingMailTest() {
		super("jp.co.fttx.rakuphotomail.activity", GallerySendingMail.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Intent i = new Intent(Intent.ACTION_MAIN);
		i.putExtra(EXTRA_ADDRESS_FROM, "tooru.oguri@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_FROM_NAME, "Togu");
		i.putExtra(EXTRA_ADDRESS_TO, "shigeharu.miyamoto@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_TO_NAME, "Miyamoto");
		i.putExtra(EXTRA_ADDRESS_REPLY_TO, "tooru.oguri@rakuphoto.ucom.local");

		mReference = new MessageReference();
		mReference.accountUuid = "8b36b53c-dbf5-468e-94d2-adc8dd3a5fde";
		mReference.folderName = "INBOX";
		mReference.uid = "500";
		i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
		setActivityIntent(i);

		mMessageBean = new MessageBean();
		// 受信メールにとってはFrom、返信メールにとってはTo
		mMessageBean.setSenderList("tooru.oguri@rakuphoto.ucom.local");
		mMessageBean.setSenderListName("Togu");
		// 受信メールにとってはTo、返信メールにとってはFrom
		mMessageBean.setToList("shigeharu.miyamoto@rakuphoto.ucom.local");
		mMessageBean.setToListName("Miyamoto");

		mActivity = getActivity();

		mMailContent = (EditText) mActivity
				.findViewById(R.id.gallery_sending_mail_content);
		mSend = (Button) mActivity.findViewById(R.id.gallery_sending_mail_send);
		mTo = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_to_address);
		mToName = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_to_name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testInitialInput() {
		assertNotNull(mMailContent);
		assertEquals("", mMailContent.getText().toString());
		assertEquals("ここにメッセージを入力して下さい", mMailContent.getHint().toString());
	}

	public void testToAddressDeault() {
		assertNotNull(mTo);
		assertEquals("shigeharu.miyamoto@rakuphoto.ucom.local", mTo.getText()
				.toString());
		assertNotNull(mToName);
		assertEquals("Miyamoto", mToName.getText().toString());
		assertEquals(View.GONE, mTo.getVisibility());
	}

	public void testSentFlag() {
		mSnetFlag = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_sent_flag);
		assertNotNull(mSnetFlag);
		assertEquals(View.VISIBLE, mSnetFlag.getVisibility());
		assertEquals("返信済み！", mSnetFlag.getText().toString());
	}

	public void testReplyMailSizeOK() throws InterruptedException {
		ActivityMonitor monitor = new ActivityMonitor(
				"jp.co.fttx.rakuphotomail.activity.GallerySendingMail", null,
				false);
		getInstrumentation().addMonitor(monitor);

		GallerySendingMail.actionReply(mActivity, mReference, mMessageBean);

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMailContent.setText("iPhone5も欲しい.");
			}
		});
		getInstrumentation().waitForIdleSync();
		Thread.sleep(3000);
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSend.performClick();
			}
		});
		getInstrumentation().waitForIdleSync();

		Activity activity = getInstrumentation().waitForMonitorWithTimeout(
				monitor, 2000);
		assertEquals(monitor.getHits(), 1);
		if (activity != null) {
			activity.finish();
		}

		assertNotNull(true);
	}

	public void testReplyMailSubjectSizeERROR() throws InterruptedException {
		ActivityMonitor monitor = new ActivityMonitor(
				"jp.co.fttx.rakuphotomail.activity.GallerySendingMail", null,
				false);
		getInstrumentation().addMonitor(monitor);

		GallerySendingMail.actionReply(mActivity, mReference, mMessageBean);

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMailContent.setText("iPhone5からが、ジョブズの本気なのだよ");
			}
		});
		getInstrumentation().waitForIdleSync();
		Thread.sleep(3000);
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSend.performClick();
			}
		});
		getInstrumentation().waitForIdleSync();

		Activity activity = getInstrumentation().waitForMonitorWithTimeout(
				monitor, 2000);
		assertEquals(monitor.getHits(), 1);
		if (activity != null) {
			activity.finish();
		}

		assertNotNull(true);
	}

}
