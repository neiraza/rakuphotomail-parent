/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.test;

import static android.view.KeyEvent.KEYCODE_A;
import static android.view.KeyEvent.KEYCODE_F;
import static android.view.KeyEvent.KEYCODE_I;
import static android.view.KeyEvent.KEYCODE_N;
import static android.view.KeyEvent.KEYCODE_O;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.activity.GallerySendingMail;
import jp.co.fttx.rakuphotomail.activity.MessageReference;
import android.app.Instrumentation;
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
		mActivity = null;
		mMailContent = null;
		mTo = null;
		mToName = null;
		mSentFlag = null;
		mSend = null;
		mInstrumentation = null;
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
		startActivity();
		setUpViews();

		assertNotNull(mMailContent);
		assertEquals("", mMailContent.getText().toString());
		assertEquals("ここにメッセージを入力して下さい", mMailContent.getHint().toString());
	}

	public void testToAddressDefault() {
		startActivity();
		setUpViews();

		assertNotNull(mTo);
		assertEquals("shigeharu.miyamoto@rakuphoto.ucom.local", mTo.getText()
				.toString());
		assertNotNull(mToName);
		assertEquals("Miyamoto", mToName.getText().toString());
		assertEquals(View.GONE, mTo.getVisibility());
	}

	public void testReplySentFlagOff() {
		startActivity();
		setUpViews();

		assertNotNull(mSentFlag);
		assertEquals(View.GONE, mSentFlag.getVisibility());
		assertEquals("返信済み！", mSentFlag.getText().toString());
	}

	public void testReplySentFlagOn() throws InterruptedException {

		startActivitySentFlagOn();
		setUpViews();

		assertNotNull(mSentFlag);
		assertEquals(View.VISIBLE, mSentFlag.getVisibility());
	}

	public void testReplyMailSizeOK() throws InterruptedException {

		startActivity();
		setUpViews();

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMailContent.requestFocus();
			}
		});
		mInstrumentation.waitForIdleSync();

		sendKeys(KEYCODE_A, KEYCODE_I, KEYCODE_F, KEYCODE_O, KEYCODE_N,
				KEYCODE_N);

		// 入力確認
		assertEquals("あいふぉん", mMailContent.getText().toString());

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSend.performClick();
			}
		});
		mInstrumentation.waitForIdleSync();

	}

	public void testReplyMailSubjectSizeERROR() throws InterruptedException {

		startActivity();
		setUpViews();

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMailContent.requestFocus();
			}
		});
		mInstrumentation.waitForIdleSync();

		sendKeys(KEYCODE_A, KEYCODE_I, KEYCODE_F, KEYCODE_O, KEYCODE_N,
				KEYCODE_N, KEYCODE_A, KEYCODE_I, KEYCODE_F, KEYCODE_O,
				KEYCODE_N, KEYCODE_N, KEYCODE_A, KEYCODE_I, KEYCODE_F,
				KEYCODE_O, KEYCODE_N, KEYCODE_N);

		// 入力確認
		assertEquals("あいふぉんあいふぉんあいふぉん", mMailContent.getText().toString());

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSend.performClick();
			}
		});
		mInstrumentation.waitForIdleSync();

	}

	private void startActivity() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.putExtra(EXTRA_ADDRESS_FROM, "tooru.oguri@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_FROM_NAME, "Togu");
		i.putExtra(EXTRA_ADDRESS_TO, "shigeharu.miyamoto@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_TO_NAME, "Miyamoto");
		i.putExtra(EXTRA_ADDRESS_REPLY_TO,
				"shigeharu.miyamoto@rakuphoto.ucom.local");
		i.putExtra(EXTRA_MESSAGE_ANSWERED, false);

		mReference = new MessageReference();
		mReference.accountUuid = "8b36b53c-dbf5-468e-94d2-adc8dd3a5fde";
		mReference.folderName = "INBOX";
		mReference.uid = "500";
		i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
		setActivityIntent(i);

		mActivity = getActivity();
	}

	private void startActivitySentFlagOn() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.putExtra(EXTRA_ADDRESS_FROM, "tooru.oguri@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_FROM_NAME, "Togu");
		i.putExtra(EXTRA_ADDRESS_TO, "shigeharu.miyamoto@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_TO_NAME, "Miyamoto");
		i.putExtra(EXTRA_ADDRESS_REPLY_TO,
				"shigeharu.miyamoto@rakuphoto.ucom.local");
		i.putExtra(EXTRA_MESSAGE_ANSWERED, true);

		mReference = new MessageReference();
		mReference.accountUuid = "8b36b53c-dbf5-468e-94d2-adc8dd3a5fde";
		mReference.folderName = "INBOX";
		mReference.uid = "500";
		i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
		setActivityIntent(i);

		mActivity = getActivity();
	}

	private void setUpViews() {
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

}
