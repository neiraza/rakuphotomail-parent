/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.test;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import jp.co.fttx.rakuphotomail.test.dummy.DummyAccounts;

/**
 * jp.co.fttx.rakuphotomail.GallerySlideShowTest.
 * 
 * @author tooru.oguri
 * @since 0.1-beta1
 * 
 */
public class GallerySlideShowTest extends
		ActivityInstrumentationTestCase2<DummyAccounts> {

	private DummyAccounts mActivity;
	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_FOLDER = "folder";


	public GallerySlideShowTest() {
		super("jp.co.fttx.rakuphotomail.test.dummy", DummyAccounts.class);
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
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testActivityObjectNotNull() {
		startActivity();
		assertNotNull(mActivity);
	}

	/**
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	private void startActivity() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		setActivityIntent(i);
		mActivity = getActivity();
	}

}
