package jp.co.fttx.rakuphotomail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AttachmentSynqReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String str = intent.getStringExtra("UID");
		Toast.makeText(context, str, Toast.LENGTH_LONG);
		
//		String action = intent.getAction();
//		String uid = intent.getStringExtra("UID");
//		Intent new_intent;
//		if (action.equals("jp.co.fttx.rakuphotomail.service.AttachmentSynqService.action")) {
//			Log.d("download_test", "AttachmentSynqReceiver#onReceive action:"
//					+ action);
//			new_intent = new Intent(context, AttachmentSynqReslutActivity.class);
//			new_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			new_intent.putExtra("UID", uid);
//			Log.d("download_test", "AttachmentSynqReceiver#onReceive action:"
//					+ action);
//			context.startActivity(new_intent);
//		}
	}
}
