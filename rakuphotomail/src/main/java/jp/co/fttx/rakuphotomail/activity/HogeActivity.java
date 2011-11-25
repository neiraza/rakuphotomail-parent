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
import jp.co.fttx.rakuphotomail.service.HogeService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HogeActivity extends Activity implements OnClickListener {

	private TextView mResultText;
	private Button mStart;
	private Button mStop;
	private Button mResult;
	private Intent intent;
	private HogeService hogeService;

	private boolean mIsBound = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("hoge", "HogeActivity#onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hoge);
		mResultText = (TextView) findViewById(R.id.hoge_text_result);
		mStart = (Button) findViewById(R.id.hoge_start);
		mStart.setOnClickListener(this);
		mStop = (Button) findViewById(R.id.hoge_stop);
		mStop.setOnClickListener(this);
		mResult = (Button) findViewById(R.id.hoge_result);
		mResult.setOnClickListener(this);
		intent = new Intent(HogeActivity.this, HogeService.class);
	}

	@Override
	public void onStart() {
		Log.d("hoge", "HogeActivity#onStart");
		super.onStart();
		// ここを削除してbindだけのテストできるお
		doServiceStart();
	}

	private void doBindService() {
		Log.d("hoge", "HogeActivity#doBind");
		if (!mIsBound) {
			mIsBound = bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);
		}
	}

	private void doUnbindService() {
		Log.d("hoge", "HogeActivity#doUnBind");
		if (mIsBound) {
			Log.d("hoge", "HogeActivity#doUnBind unbindService");
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public void onDestroy() {
		Log.d("hoge", "HogeActivity#onDestroy");
		super.onDestroy();
		// ここを削除してbindだけのテストできるお
		doServiceStop();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("hoge", "ServiceConnection#onServiceConnected");
			hogeService = ((HogeService.HogeBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("hoge", "ServiceConnection#onServiceDisconnected");
			hogeService = null;
		}
	};

	private void doServiceStart() {
		Log.d("hoge", "HogeActivity#doServiceStart");
		intent.putExtra("message", "計算開始！");
		startService(intent);
	}

	private void doServiceStop() {
		Log.d("hoge", "HogeActivity#doServiceStop");
		hogeService.stopSelf();
	}

	private void doResult() {
		Log.d("hoge", "HogeActivity#doResult");
		int r = hogeService.getResult();
		Log.d("hoge", "HogeActivity#doResult r:" + r);
		mResultText.setText(String.valueOf(r));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hoge_start:
			Log.d("hoge", "HogeActivity#onClick start");
			doBindService();
			break;
		case R.id.hoge_stop:
			Log.d("hoge", "HogeActivity#onClick stop");
			doUnbindService();
			break;
		case R.id.hoge_result:
			Log.d("hoge", "HogeActivity#onClick result");
			doResult();
			break;
		default:
			break;
		}
	}
}
