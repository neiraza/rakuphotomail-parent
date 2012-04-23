package jp.co.fttx.rakuphotomail.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;

public class PushService extends CoreService {
    private static String START_SERVICE = "jp.co.fttx.rakuphotomail.service.PushService.startService";
    private static String STOP_SERVICE = "jp.co.fttx.rakuphotomail.service.PushService.stopService";

    public static void startService(Context context) {
        Intent i = new Intent();
        i.setClass(context, PushService.class);
        i.setAction(PushService.START_SERVICE);
        addWakeLock(context, i);
        context.startService(i);
    }

    public static void stopService(Context context) {
        Intent i = new Intent();
        i.setClass(context, PushService.class);
        i.setAction(PushService.STOP_SERVICE);
        addWakeLock(context, i);
        context.startService(i);
    }

    @Override
    public void startService(Intent intent, int startId) {
        if (START_SERVICE.equals(intent.getAction())) {
            if (RakuPhotoMail.DEBUG)
                Log.i(RakuPhotoMail.LOG_TAG, "PushService started with startId = " + startId);
        } else if (STOP_SERVICE.equals(intent.getAction())) {
            if (RakuPhotoMail.DEBUG)
                Log.i(RakuPhotoMail.LOG_TAG, "PushService stopping with startId = " + startId);
            stopSelf(startId);
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
