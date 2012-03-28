package jp.co.fttx.rakuphotomail.rakuraku.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta3
 */
public class RakuPhotoConnectivityCheck {
    private RakuPhotoConnectivityCheck() {
    }

    public static boolean isConnectivity(Context context) {
        Log.d("abokado", "RakuPhotoConnectivityCheck#isConnectivity start");
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        Log.d("abokado", "RakuPhotoConnectivityCheck#isConnectivity mobile:" + mobile.name());
        Log.d("abokado", "RakuPhotoConnectivityCheck#isConnectivity wifi:" + wifi.name());
        return (mobile == NetworkInfo.State.CONNECTED) || (wifi == NetworkInfo.State.CONNECTED);
    }
}
