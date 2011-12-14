package jp.co.fttx.rakuphotomail.activity;

import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqReceiver;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqService;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqService.AttachmentSynqBinder;
import jp.co.fttx.rakuphotomail.service.GallerySlideReceiver;
import jp.co.fttx.rakuphotomail.service.GallerySlideService;
import jp.co.fttx.rakuphotomail.service.GallerySlideService.GallerySlideBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class RakuPhotoMenuSelecter extends Activity {
    private ImageView mMail;
    private ImageView mOther;
    private Context mContext;

    private AttachmentSynqService synqService;
    private GallerySlideService slideService;
    private boolean mIsBound = false;
    private AttachmentSynqReceiver attachmentReceiver = new AttachmentSynqReceiver();
    private GallerySlideReceiver slideReceiver = new GallerySlideReceiver();

    private Intent intent;

    private void doBindService() {
        Log.d("maguro", "RakuPhotoMenuSelecter#doBindService start");
        if (!mIsBound) {
            mIsBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            IntentFilter attachmenFilter = new IntentFilter(AttachmentSynqService.ACTION);
            registerReceiver(attachmentReceiver, attachmenFilter);
            IntentFilter slideFilter = new IntentFilter(GallerySlideService.ACTION);
            registerReceiver(slideReceiver, slideFilter);
        }
        Log.d("maguro", "RakuPhotoMenuSelecter#doBindService end");
    }

    private void doUnbindService() {
        Log.d("maguro", "RakuPhotoMenuSelecter#doUnBindService start");
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
            unregisterReceiver(attachmentReceiver);
            unregisterReceiver(slideReceiver);
        }
        Log.d("maguro", "RakuPhotoMenuSelecter#doUnBindService end");
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("maguro", "RakuPhotoMenuSelecter#mConnection ServiceConnection#onServiceConnected start");
            if (service instanceof AttachmentSynqBinder) {
                Log.d("maguro",
                    "RakuPhotoMenuSelecter#mConnection ServiceConnection#onServiceConnected AttachmentSynqBinder");
                synqService = ((AttachmentSynqService.AttachmentSynqBinder) service).getService();
            } else if (service instanceof GallerySlideBinder) {
                Log.d("maguro",
                    "RakuPhotoMenuSelecter#mConnection ServiceConnection#onServiceConnected GallerySlideBinder");
                slideService = ((GallerySlideService.GallerySlideBinder) service).getService();
            } else {
                Log.d("maguro",
                    "RakuPhotoMenuSelecter#mConnection ServiceConnection#onServiceConnected service?");
            }
            Log.d("maguro", "RakuPhotoMenuSelecter#mConnection ServiceConnection#onServiceConnected end");
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("maguro",
                "RakuPhotoMenuSelecter#mConnection ServiceConnection#onServiceDisconnected start");
            synqService = null;
            slideService = null;
            Log.d("maguro",
                "RakuPhotoMenuSelecter#mConnection ServiceConnection#onServiceDisconnected end");
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        Log.d("maguro", "RakuPhotoMenuSelecter#onCreate start");
        mContext = this;
        super.onCreate(icicle);
        setContentView(R.layout.rakuphoto_top);

        intent = new Intent(mContext, GallerySlideService.class);
        Log.d("maguro", "RakuPhotoMenuSelecter#onCreate intent.getClass():" + intent.getClass().toString());
        Log.d("maguro", "RakuPhotoMenuSelecter#onCreate intent.getClass():" + intent.getClass());
        intent.setClass(mContext, AttachmentSynqService.class);
        Log.d("maguro", "RakuPhotoMenuSelecter#onCreate intent.getClass():" + intent.getClass().toString());
        Log.d("maguro", "RakuPhotoMenuSelecter#onCreate intent.getClass():" + intent.getClass());

        doBindService();

        mMail = (ImageView) findViewById(R.id.rakuphoto_mail);

        mMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("maguro", "RakuPhotoMenuSelecter#onClick mMail");
//                DummyAccounts.listAccounts(mContext);
            }
        });

        mOther = (ImageView) findViewById(R.id.rakuphoto_other);
        mOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("maguro", "RakuPhotoMenuSelecter#onClick mOther");
                onOther();
            }
        });
        Log.d("maguro", "RakuPhotoMenuSelecter#onCreate end");
    }

    @Override
    public void onDestroy() {
        Log.d("maguro", "RakuPhotoMenuSelecter#onDestroy start");
        doUnbindService();
        Log.d("maguro", "RakuPhotoMenuSelecter#onDestroy end");
    }

    private void onOther() {
        Log.d("maguro", "RakuPhotoMenuSelecter#onOther start");

        synqService.onFuga();
        slideService.onHoge();
        Log.d("maguro", "RakuPhotoMenuSelecter#onOther end");
    }
}
