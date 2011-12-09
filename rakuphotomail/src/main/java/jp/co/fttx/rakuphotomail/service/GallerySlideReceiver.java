package jp.co.fttx.rakuphotomail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GallerySlideReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("maguro", "GallerySlideReceiver#onReceive start");
        String action = intent.getAction();
        if ("jp.co.fttx.rakuphotomail.service.GallerySlideService.action".equals(action)) {
            Log.d("maguro", "GallerySlideReceiver#onReceive action OK");
        } else {
            Log.d("maguro", "GallerySlideReceiver#onReceive action NG");
        }
        Log.d("maguro", "GallerySlideReceiver#onReceive end");
    }
}
