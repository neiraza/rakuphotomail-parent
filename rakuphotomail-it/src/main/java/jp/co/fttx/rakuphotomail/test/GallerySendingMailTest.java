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
import jp.co.fttx.rakuphotomail.mail.Flag;
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
 * @since 0.1-beta1
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

	/**
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 * @author tooru.oguri
	 * @since 0.1-beta1
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

	/**
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * null check(mMailContent).
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testNotNullMMailContent() {
		startActivity();
		setUpViews();

		assertNotNull(mMailContent);
	}
	
	/**
	 * null check(mTo).
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testNotNullMTo() {
		startActivity();
		setUpViews();
		
		assertNotNull(mTo);
	}
	
	/**
	 * null check(mSend).
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testNotNullMSend() {
		startActivity();
		setUpViews();
		
		assertNotNull(mSend);
	}
	
	/**
	 * null check(mSentFlag).
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testNotNullMSentFlag() {
		startActivity();
		setUpViews();
		
		assertNotNull(mSentFlag);
	}
	
	/**
	 * null check(mToName).
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testNotNullMToName() {
		startActivity();
		setUpViews();
		
		assertNotNull(mToName);
	}
	
	/**
	 * 未入力チェック（mMailContent）.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testInitialInput() {
		startActivity();
		setUpViews();
		
		assertEquals("", mMailContent.getText().toString());
	}
	
	/**
	 * 未入力チェック（mMailContent）.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testInitialInputHint() {
		startActivity();
		setUpViews();
		
		assertEquals("ここにメッセージを入力して下さい", mMailContent.getHint().toString());
	}
	
	/**
	 * 宛先名表示内容チェック.<br>
	 * 宛先名が想定通りの文言で表示されるか確認する.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testToName() {
		startActivity();
		setUpViews();

		assertEquals("Miyamoto", mToName.getText().toString());
	}
	
	/**
	 * 宛先名表示チェック.<br>
	 * 宛先名が存在する場合は表示するか確認する.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testToNameVisiblity() {
		startActivity();
		setUpViews();
		
		assertEquals(View.VISIBLE, mToName.getVisibility());
	}
	
	/**
	 * 宛先名非表示チェック.
	 * 宛先名が存在しない場合は表示されない事を確認する.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testToNameGone() {
		startActivityDefaultTo();
		setUpViews();
		
		assertEquals(View.GONE, mToName.getVisibility());
	}

	/**
	 * 宛先メールアドレス表示内容チェック.
	 * 宛先メールアドレスが想定通りの文言で表示されるか確認する.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testTo() {
		startActivityDefaultTo();
		setUpViews();

		assertEquals("shigeharu.miyamoto@rakuphoto.ucom.local", mTo.getText()
				.toString());
	}
	
	/**
	 * 宛先メールアドレス表示チェック.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testToAddressDefault() {
		startActivityDefaultTo();
		setUpViews();
		
		assertEquals(View.VISIBLE, mTo.getVisibility());
	}
	
	/**
	 * 宛先メールアドレス非表示チェック.<br>
	 * 宛先名が存在する場合はメールアドレスを表示されない事を確認する.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testToGone() {
		startActivity();
		setUpViews();
		
		assertEquals(View.GONE, mTo.getVisibility());
	}

	/**
	 * 返信済みフラグ非表示チェック（返信無し）.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testReplySentFlagOff() {
		startActivity();
		setUpViews();

		assertEquals(View.GONE, mSentFlag.getVisibility());
	}

	/**
	 * 返信済みフラグ表示文言チェック（返信有り）.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testReplySentFlagOnStr() throws InterruptedException {

		startActivitySentFlagOn();
		setUpViews();

		assertEquals("返信済み！", mSentFlag.getText().toString());
	}
	
	/**
	 * 返信済みフラグ表示チェック（返信有り）.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testReplySentFlagOn() throws InterruptedException {
		
		startActivitySentFlagOn();
		setUpViews();
		
		assertEquals(View.VISIBLE, mSentFlag.getVisibility());
	}

	/**
	 * 文字入力チェック（正常、入力可能文字数以下）.<br>
	 * 但し、使用するテスト環境（エミュレータ、実機）によっては、<br>
	 * 日本語使用が不可能な為、失敗する可能性がある（どうしようもない）
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
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

	/**
	 * 文字入力チェック（正常、入力可能文字数（17文字））.<br>
	 * 但し、使用するテスト環境（エミュレータ、実機）によっては、<br>
	 * 日本語使用が不可能な為、失敗する可能性がある（どうしようもない）
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testReplyMailSizeOKBoundaryvalue() throws InterruptedException {

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
				KEYCODE_O, KEYCODE_N, KEYCODE_N, KEYCODE_A, KEYCODE_I);

		// 入力確認
		String expected = "あいふぉんあいふぉんあいふぉんあい";
		assertEquals(17, expected.length());
		assertEquals(expected, mMailContent.getText().toString());

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSend.performClick();
			}
		});
		mInstrumentation.waitForIdleSync();

	}

	/**
	 * 文字入力チェック（異常、未入力時）.
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testReplyMailSubjectNoInput() throws InterruptedException {

		startActivity();
		setUpViews();

		// 入力確認
		assertEquals("", mMailContent.getText().toString());

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSend.performClick();
			}
		});
		mInstrumentation.waitForIdleSync();

	}

	/**
	 * 文字入力チェック（異常、入力可能文字数超過時）.<br>
	 * 但し、使用するテスト環境（エミュレータ、実機）によっては、<br>
	 * 日本語使用が不可能な為、失敗する可能性がある（どうしようもない）
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testReplyMailSubjectSizeOVER() throws InterruptedException {

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

	/**
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
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
		mReference.accountUuid = "a848514f-b6a4-447f-9f8a-8632cd9c8316";
		mReference.folderName = "INBOX";
		mReference.uid = "500";
		mReference.flag = Flag.ANSWERED;
		i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
		setActivityIntent(i);

		mActivity = getActivity();
	}

	/**
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	private void startActivityDefaultTo() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.putExtra(EXTRA_ADDRESS_FROM, "tooru.oguri@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_FROM_NAME, "Togu");
		i.putExtra(EXTRA_ADDRESS_TO, "shigeharu.miyamoto@rakuphoto.ucom.local");
		i.putExtra(EXTRA_ADDRESS_TO_NAME, "");
		i.putExtra(EXTRA_ADDRESS_REPLY_TO,
				"shigeharu.miyamoto@rakuphoto.ucom.local");
		i.putExtra(EXTRA_MESSAGE_ANSWERED, false);

		mReference = new MessageReference();
		mReference.accountUuid = "a848514f-b6a4-447f-9f8a-8632cd9c8316";
		mReference.folderName = "INBOX";
		mReference.uid = "500";
		mReference.flag = Flag.ANSWERED;
		i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
		setActivityIntent(i);

		mActivity = getActivity();
	}

	/**
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
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
		mReference.accountUuid = "a848514f-b6a4-447f-9f8a-8632cd9c8316";
		mReference.folderName = "INBOX";
		mReference.uid = "500";
		mReference.flag = Flag.ANSWERED;
		i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
		setActivityIntent(i);

		mActivity = getActivity();
	}

	/**
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
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
