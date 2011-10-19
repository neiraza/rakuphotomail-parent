/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.activity.GallerySendingMail;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GallerySendingMailTest.
 * @author tooru.oguri
 *
 */
public class GallerySendingMailTest extends
		ActivityInstrumentationTestCase2<GallerySendingMail> {
	
	private GallerySendingMail mActivity;
	private EditText mMailContent;
	private TextView mMailToAddress;
	private TextView mSnetFlag;

	public GallerySendingMailTest(){
		super("jp.co.fttx.rakuphotomail.activity" , GallerySendingMail.class);
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testHoge(){
		assertNotNull(true);
	}
	
	public void testInitialInput(){
		mMailContent = (EditText)mActivity.findViewById(R.id.gallery_sending_mail_content);
		assertNotNull(mMailContent);
		assertEquals("", mMailContent.getText().toString());
		assertEquals("ここにメッセージを入力して下さい", mMailContent.getHint().toString());
	}
	public void testToAddressDeault(){
		mMailToAddress = (TextView)mActivity.findViewById(R.id.gallery_sending_mail_to_address);
		assertNotNull(mMailToAddress);
		assertEquals("宮本さん５６７８９０１２３４５６７８９０１２３４５６７８９０", mMailToAddress.getText().toString());
	}
	public void testSentFlag(){
		mSnetFlag = (TextView)mActivity.findViewById(R.id.gallery_sending_mail_sent_flag);
		assertNotNull(mSnetFlag);
		assertEquals(View.VISIBLE, mSnetFlag.getVisibility());
		assertEquals("返信済み！", mSnetFlag.getText().toString());
	}

}
