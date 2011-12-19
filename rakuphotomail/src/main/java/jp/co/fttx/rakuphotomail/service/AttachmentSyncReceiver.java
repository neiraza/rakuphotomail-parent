package jp.co.fttx.rakuphotomail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AttachmentSyncReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO debug
        Log.d("maguro", "AttachmentSyncReceiver#onReceive start");
        Log.d("abcdef", "AttachmentSyncReceiver#onReceive");
        String action = intent.getAction();
        Log.d("maguro", "AttachmentSyncReceiver#onReceive action:" + action);
        if ("jp.co.fttx.rakuphotomail.service.AttachmentSyncService.action".equals(action)) {
            String uid = intent.getStringExtra("UID");
            Log.d("maguro", "AttachmentSyncReceiver#onReceive uid:" + uid);
        } else {
            // TODO debug
            Log.d("maguro", "AttachmentSyncReceiver#onReceive illegal action");
        }
        Log.d("maguro", "AttachmentSyncReceiver#onReceive end");
    }
}
