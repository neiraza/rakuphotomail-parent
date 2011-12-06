/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.activity.GallerySlideShow;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

/**
 * GallerySlideShowTest.
 * 
 * @author tooru.oguri
 * @since 0.1-beta1
 * 
 */
public class GallerySlideShowTest extends
		ActivityInstrumentationTestCase2<GallerySlideShow> {

	private GallerySlideShow mActivity;
	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_FOLDER = "folder";

	public GallerySlideShowTest() {
		super("jp.co.fttx.rakuphotomail.activity", GallerySlideShow.class);
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
	 * null check(mMailContent).
	 * 
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	public void testHoge() {
//		startActivity();
//		assertNotNull(mActivity);
	}

	/**
	 * @author tooru.oguri
	 * @since 0.1-beta1
	 */
	private void startActivity() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.putExtra(EXTRA_ACCOUNT, "a848514f-b6a4-447f-9f8a-8632cd9c8316");
		i.putExtra(EXTRA_FOLDER, "INBOX");
		setActivityIntent(i);
		mActivity = getActivity();
	}

}
