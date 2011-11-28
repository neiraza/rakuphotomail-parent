/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.fttx.rakuphotomail.activity;

import jp.co.fttx.rakuphotomail.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class AttachmentSynqReslutActivity extends Activity implements
		OnClickListener {

	private ImageView mImage;
	private Button mFinish;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("download_test", "AttachmentSynqReslutActivity#onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_reslut);
		mImage = (ImageView) findViewById(R.id.download_result_image_view);
		mFinish = (Button) findViewById(R.id.download_result_finish);
		mFinish.setOnClickListener(this);
		String uid = getIntent().getStringExtra("UID");
		Toast.makeText(this, uid, Toast.LENGTH_SHORT).show();
		Log.d("download_test", "AttachmentSynqReslutActivity#onCreate uid"
				+ uid);
	}

	@Override
	public void onStart() {
		Log.d("download_test", "AttachmentSynqReslutActivity#onStart");
		super.onStart();
	}

	@Override
	public void onDestroy() {
		Log.d("download_test", "AttachmentSynqReslutActivity#onDestroy");
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		onDestroy();
	}

}
