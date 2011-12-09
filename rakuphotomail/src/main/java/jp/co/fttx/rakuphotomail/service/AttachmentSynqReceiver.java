package jp.co.fttx.rakuphotomail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AttachmentSynqReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO debug
        Log.d("maguro", "AttachmentSynqReceiver#onReceive start");
        String action = intent.getAction();
        Log.d("maguro", "AttachmentSynqReceiver#onReceive action:" + action);
        if ("jp.co.fttx.rakuphotomail.service.AttachmentSynqService.action".equals(action)) {
            String uid = intent.getStringExtra("UID");
            Log.d("maguro", "AttachmentSynqReceiver#onReceive uid:" + uid);
        } else {
            // TODO debug
            Log.d("maguro", "AttachmentSynqReceiver#onReceive illegal action");
        }
        Log.d("maguro", "AttachmentSynqReceiver#onReceive end");
    }
}
