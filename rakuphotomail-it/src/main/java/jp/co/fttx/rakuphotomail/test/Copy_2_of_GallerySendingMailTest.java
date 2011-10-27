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
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import static android.view.KeyEvent.*;

/**
 * GallerySendingMailTest.
 * 
 * @author tooru.oguri
 * 
 */
public class Copy_2_of_GallerySendingMailTest extends
		ActivityInstrumentationTestCase2<GallerySendingMail> {

	private GallerySendingMail mActivity;
	private EditText mMailContent;
	private TextView mTo;
	private TextView mToName;
	private TextView mSentFlag;
	private Button mSend;
	private Instrumentation mInstrumentation;
	private static final String EXTRA_ADDRESS_FROM = "addressFrom";
	private static final String EXTRA_ADDRESS_FROM_NAME = "addressFromName";
	private static final String EXTRA_ADDRESS_TO = "addressTo";
	private static final String EXTRA_ADDRESS_TO_NAME = "addressToName";
	private static final String EXTRA_ADDRESS_REPLY_TO = "addressReplyTo";
	private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
	private static final String EXTRA_MESSAGE_ANSWERED = "answered";
	MessageReference mReference;

	public Copy_2_of_GallerySendingMailTest() {
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
		i.putExtra(EXTRA_MESSAGE_ANSWERED, false);

		mReference = new MessageReference();
		mReference.accountUuid = "8b36b53c-dbf5-468e-94d2-adc8dd3a5fde";
		mReference.folderName = "INBOX";
		mReference.uid = "500";
		i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
		setActivityIntent(i);

		mActivity = getActivity();

		mMailContent = (EditText) mActivity
				.findViewById(R.id.gallery_sending_mail_content);
		mSend = (Button) mActivity.findViewById(R.id.gallery_sending_mail_send);
		mTo = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_to_address);
		mToName = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_to_name);
		mSentFlag = (TextView) mActivity
				.findViewById(R.id.gallery_sending_mail_sent_flag);

		mInstrumentation = getInstrumentation();
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

	public void testReplyMailSizeOK() throws InterruptedException {

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMailContent.requestFocus();
			}
		});
		getInstrumentation().waitForIdleSync();

		sendKeys(KEYCODE_S, KEYCODE_I, KEYCODE_N, KEYCODE_N, KEYCODE_S, KEYCODE_U, KEYCODE_K, KEYCODE_E);

		assertEquals("しんすけ", mMailContent.getText().toString());

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSend.performClick();
			}
		});
		getInstrumentation().waitForIdleSync();

		assertEquals(View.GONE, mSentFlag.getVisibility());

	}

}
