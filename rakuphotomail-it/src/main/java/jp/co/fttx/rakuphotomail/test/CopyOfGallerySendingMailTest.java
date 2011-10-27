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
public class CopyOfGallerySendingMailTest extends
		ActivityInstrumentationTestCase2<GallerySendingMail> {

	private static final String EXTRA_ADDRESS_FROM = "addressFrom";
	private static final String EXTRA_ADDRESS_FROM_NAME = "addressFromName";
	private static final String EXTRA_ADDRESS_TO = "addressTo";
	private static final String EXTRA_ADDRESS_TO_NAME = "addressToName";
	private static final String EXTRA_ADDRESS_REPLY_TO = "addressReplyTo";
	private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
	private static final String EXTRA_MESSAGE_ANSWERED = "answered";
	MessageReference mReference;

	public CopyOfGallerySendingMailTest() {
		super("jp.co.fttx.rakuphotomail.activity", GallerySendingMail.class);
	}

	GallerySendingMail mActivity;
	TextView mSentFlag;
	Intent i;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		i = new Intent(Intent.ACTION_MAIN);
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

	EditText mMailContent;
	Button mSend;

	public void testReplyMailSizeOK() throws InterruptedException {
		
		i.putExtra(EXTRA_MESSAGE_ANSWERED, false);

		mActivity = getActivity();

		mMailContent = (EditText) mActivity
				.findViewById(R.id.gallery_sending_mail_content);

		mSend = (Button) mActivity.findViewById(R.id.gallery_sending_mail_send);
		mSentFlag = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_sent_flag);

		mMailContent = (EditText) mActivity
				.findViewById(R.id.gallery_sending_mail_content);

		mMailContent = (EditText) mActivity
				.findViewById(R.id.gallery_sending_mail_content);

		mSend = (Button) mActivity.findViewById(R.id.gallery_sending_mail_send);
		mSentFlag = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_sent_flag);

		ActivityMonitor monitor = new ActivityMonitor(
				"jp.co.fttx.rakuphotomail.activity.GallerySendingMail", null,
				false);
		getInstrumentation().addMonitor(monitor);

		MessageBean messageBean = new MessageBean();
		messageBean.setSenderList("tooru.oguri@rakuphoto.ucom.local");
		messageBean.setSenderListName("Togu");
		messageBean.setToList("shigeharu.miyamoto@rakuphoto.ucom.local");
		messageBean.setToListName("Miyamoto");
		messageBean.setFlagAnswered(false);
		GallerySendingMail.actionReply(mActivity, mReference, messageBean);

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

		assertEquals(View.GONE, mSentFlag.getVisibility());

		assertNotNull(true);

	}

	public void testInitialInput() {
		assertNotNull(mMailContent);
		assertEquals("", mMailContent.getText().toString());
		assertEquals("ここにメッセージを入力して下さい", mMailContent.getHint().toString());
	}

	public void testReplyMailSubjectSizeERROR() throws InterruptedException {

		i.putExtra(EXTRA_MESSAGE_ANSWERED, true);
		
		mActivity = getActivity();

		mMailContent = (EditText) mActivity
				.findViewById(R.id.gallery_sending_mail_content);

		mSend = (Button) mActivity.findViewById(R.id.gallery_sending_mail_send);
		mSentFlag = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_sent_flag);

		ActivityMonitor monitor = new ActivityMonitor(
				"jp.co.fttx.rakuphotomail.activity.GallerySendingMail", null,
				false);
		getInstrumentation().addMonitor(monitor);

		MessageBean messageBean = new MessageBean();
		messageBean.setSenderList("tooru.oguri@rakuphoto.ucom.local");
		messageBean.setSenderListName("Togu");
		messageBean.setToList("shigeharu.miyamoto@rakuphoto.ucom.local");
		messageBean.setToListName("Miyamoto");
		messageBean.setFlagAnswered(true);
		GallerySendingMail.actionReply(mActivity, mReference, messageBean);

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMailContent.setText("iPhone5から");
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

		// UTでテストごとに、sentFlag.getVisibility()の値が変わってくるはずだが、
		assertEquals(View.VISIBLE, mSentFlag.getVisibility());

		assertNotNull(true);
	}

}
