package jp.co.fttx.rakuphotomail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.store.StorageManager;

/**
 * That BroadcastReceiver is only interested in UNMOUNT events.
 *
 * <p>
 * Code was separated from {@link StorageReceiver} because we don't want that
 * receiver to be statically defined in manifest.
 * </p>
 */
public class StorageGoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        final Uri uri = intent.getData();

        if (uri == null || uri.getPath() == null) {
            return;
        }

        if (RakuPhotoMail.DEBUG) {
            Log.v(RakuPhotoMail.LOG_TAG, "StorageGoneReceiver: " + intent.toString());
        }

        final String path = uri.getPath();

        if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
            StorageManager.getInstance(RakuPhotoMail.app).onBeforeUnmount(path);
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            StorageManager.getInstance(RakuPhotoMail.app).onAfterUnmount(path);
        }
    }

}
