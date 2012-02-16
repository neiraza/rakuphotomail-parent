/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.os.Bundle;
import android.view.Window;
import jp.co.fttx.rakuphotomail.R;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class DummyStop extends RakuPhotoActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show_postcard1_stop);
    }
  }
