package jp.co.fttx.rakuphotomail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.activity.GallerySlideStop;

public class AttachmentSyncReceiver extends BroadcastReceiver {
    /**
     * Intent get/put account uuid
     */
    private static final String EXTRA_ACCOUNT = "account";
    /**
     * Intent get/put folder name
     */
    private static final String EXTRA_FOLDER = "folder";
    /**
     * Intent get/put folder name
     */
    private static final String EXTRA_UID = "uid";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("maguro", "AttachmentSyncReceiver#onReceive start");
        String action = intent.getAction();
        Log.d("maguro", "AttachmentSyncReceiver#onReceive action:" + action);
        if (AttachmentSyncService.ACTION_SLIDE_SHOW.equals(action)) {
            Log.d("maguro", "AttachmentSyncReceiver#onReceive SLIDE_SHOW download...");
            String uid = intent.getStringExtra(EXTRA_UID);
            Log.d("maguro", "AttachmentSyncReceiver#onReceive uid:" + uid);
        } else if (AttachmentSyncService.ACTION_SLIDE_SHOW_STOP.equals(action)) {
            Log.d("maguro", "AttachmentSyncReceiver#onReceive SLIDE_SHOW_STOP download...");
            Intent i = new Intent(context, GallerySlideStop.class);
            i.putExtra(EXTRA_UID, intent.getStringExtra(EXTRA_UID));
            i.putExtra(EXTRA_ACCOUNT, Preferences.getPreferences(context).getAccount(intent.getStringExtra(EXTRA_ACCOUNT)).getUuid());
            i.putExtra(EXTRA_FOLDER, intent.getStringExtra(EXTRA_FOLDER));
            context.startActivity(i);
            Log.d("maguro", "AttachmentSyncReceiver#onReceive uid:" + intent.getStringExtra(EXTRA_UID));
        } else {
            Log.d("maguro", "AttachmentSyncReceiver#onReceive illegal action");
        }
        Log.d("maguro", "AttachmentSyncReceiver#onReceive end");
    }
}
