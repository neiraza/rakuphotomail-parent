package jp.co.fttx.rakuphotomail.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class HogeService extends Service {

	private volatile int result = 0;
	private int tmp = 0;
	private Thread fibonacciThread;
	private Runnable fibonacci;
	private boolean roop = true;
	private String message;

	@Override
	public void onCreate() {
		Log.d("hoge", "HogeService#onCreate");
		super.onCreate();
		result = 0 + 1;
		startFibonacci();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("hoge", "HogeService#onStart");
		message = intent.getStringExtra("message");
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		Log.d("hoge", "HogeService#onDestroy");
		roop = false;
		try {
			fibonacciThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.d("hoge", "HogeService end");
	}

	public class HogeBinder extends Binder {
		public HogeService getService() {
			Log.d("hoge", "HogeBinder#getService");
			return HogeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("hoge", "HogeService#onBind");
		fibonacciThread.start();
		Log.d("hoge", "HogeService#onBind fibonacciThread:" + fibonacciThread.isAlive());
		return mBinder;
	}
	
	private final IBinder mBinder = new HogeBinder();

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("hoge", "HogeService#onUnbind");
		boolean b = super.onUnbind(intent);
		Log.d("hoge", "HogeService#onUnbind b:" + b);
		return b;
	}

	public void startFibonacci() {
		Log.d("hoge", "HogeBinder#startFibonacci");
		fibonacci = new Runnable() {
			@Override
			public void run() {
				while (roop) {
					Log.d("hoge", "計算中...");
					tmp = result;
					result = result + tmp;
					try {
						Log.d("hoge", "HogeBinder#startFibonacci result:" + result);
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		fibonacciThread = new Thread(fibonacci);
		
	}

	public int getResult() {
		Log.d("hoge", "HogeService#getResult");
		return result;
	}
}
