package jp.co.fttx.rakuphotomail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.store.StorageManager;

/**
 * That BroadcastReceiver is only interested in MOUNT events.
 */
public class StorageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        final Uri uri = intent.getData();

        if (uri == null || uri.getPath() == null) {
            return;
        }

        if (RakuPhotoMail.DEBUG) {
            Log.v(RakuPhotoMail.LOG_TAG, "StorageReceiver: " + intent.toString());
        }

        final String path = uri.getPath();

        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            StorageManager.getInstance(RakuPhotoMail.app).onMount(path,
                    intent.getBooleanExtra("read-only", true));
        }
    }

}
