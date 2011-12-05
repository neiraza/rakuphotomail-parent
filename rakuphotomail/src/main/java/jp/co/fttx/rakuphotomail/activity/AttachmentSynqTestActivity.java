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

import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqReceiver;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class AttachmentSynqTestActivity extends Activity implements
		OnClickListener {

	private Button mStart;
	private Button mStop;
	private Button mResult;
	private EditText mInputUid;
	private TextView mDispUid;
	private ImageView mImage;
	private Intent intent;
	private AttachmentSynqService synqService;
	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_FOLDER = "folder";
	private Account mAccount;
	private String mFolderName;

	private boolean mIsBound = false;

	private AttachmentSynqReceiver receiver = new AttachmentSynqReceiver();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("download_test", "AttachmentSynqTestActivity#onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_test);
		
		mAccount = Preferences.getPreferences(this).getAccount(
				getIntent().getStringExtra(EXTRA_ACCOUNT));
		mFolderName = getIntent().getStringExtra(EXTRA_FOLDER);

		mStart = (Button) findViewById(R.id.download_start);
		mStart.setOnClickListener(this);
		mStop = (Button) findViewById(R.id.download_stop);
		mStop.setOnClickListener(this);
		mResult = (Button) findViewById(R.id.download_result);
		mResult.setOnClickListener(this);
		mInputUid = (EditText) findViewById(R.id.download_test_input_uid);
		mInputUid.addTextChangedListener(watchHandler);
		mDispUid = (TextView) findViewById(R.id.download_test_uid_text);
		mImage = (ImageView) findViewById(R.id.download_test_image_view);
		intent = new Intent(AttachmentSynqTestActivity.this,
				AttachmentSynqService.class);
	}

	private TextWatcher watchHandler = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mDispUid.setText(s);
		}
	};

	public static void actionHandleFolder(Context context, Account account,
			String folder) {
		Log.d("download_test", "AttachmentSynqTestActivity#actionHandleFolder");
		Intent intent = actionHandleFolderIntent(context, account, folder);
		context.startActivity(intent);
	}

	public static Intent actionHandleFolderIntent(Context context,
			Account account, String folder) {
		Log.d("download_test",
				"AttachmentSynqTestActivity#actionHandleFolderIntent");
		Intent intent = new Intent(context, AttachmentSynqTestActivity.class);
		if (account != null) {
			Log.d("download_test",
					"AttachmentSynqTestActivity#actionHandleFolderIntent account.getUuid():"
							+ account.getUuid());
			intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
		}
		if (folder != null) {
			Log.d("download_test",
					"AttachmentSynqTestActivity#actionHandleFolderIntent folder:"
							+ folder);
			intent.putExtra(EXTRA_FOLDER, folder);
		}
		return intent;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
		outState.putString(EXTRA_FOLDER, mFolderName);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mAccount = Preferences.getPreferences(this).getAccount(
				savedInstanceState.getString(EXTRA_ACCOUNT));
		mFolderName = savedInstanceState.getString(EXTRA_FOLDER);
	}

	@Override
	public void onStart() {
		Log.d("download_test", "AttachmentSynqTestActivity#onStart");
		super.onStart();
		// ここを削除してbindだけのテストできるお
		// doServiceStart();
	}

	private void doBindService() {
		Log.d("download_test", "AttachmentSynqTestActivity#doBind");
		if (!mIsBound) {
			mIsBound = bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);

			IntentFilter filter = new IntentFilter(AttachmentSynqService.ACTION);
			registerReceiver(receiver, filter);
		}
	}

	private void doUnbindService() {
		Log.d("download_test", "AttachmentSynqTestActivity#doUnBind");
		if (mIsBound) {
			Log.d("hoge", "AttachmentSynqTestActivity#doUnBind unbindService");
			unbindService(mConnection);
			mIsBound = false;
			unregisterReceiver(receiver);
		}
	}

	@Override
	public void onDestroy() {
		Log.d("download_test", "AttachmentSynqTestActivity#onDestroy");
		super.onDestroy();
		// ここを削除してbindだけのテストできるお
		// doServiceStop();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("download_test", "ServiceConnection#onServiceConnected");
			synqService = ((AttachmentSynqService.AttachmentSynqBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("download_test", "ServiceConnection#onServiceDisconnected");
			synqService = null;
		}
	};

	// private void doServiceStart() {
	// Log.d("hoge", "HogeActivity#doServiceStart");
	// intent.putExtra("message", "計算開始！");
	// startService(intent);
	// }
	//
	// private void doServiceStop() {
	// Log.d("hoge", "HogeActivity#doServiceStop");
	// synqService.stopSelf();
	// }

	private void doDownload() throws MessagingException {
		Log.d("download_test", "AttachmentSynqTestActivity#doDownload");
		Log.d("download_test",
				"AttachmentSynqTestActivity#doDownload mAccount:" + mAccount);
		synqService.onDownload(mAccount, mFolderName, mDispUid.getText()
				.toString());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.download_start:
			Log.d("download_test", "AttachmentSynqTestActivity#onClick start");
			doBindService();
			break;
		case R.id.download_stop:
			Log.d("download_test", "AttachmentSynqTestActivity#onClick stop");
			doUnbindService();
			break;
		case R.id.download_result:
			Log.d("download_test", "AttachmentSynqTestActivity#onClick result");
			try {
				doDownload();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
}
