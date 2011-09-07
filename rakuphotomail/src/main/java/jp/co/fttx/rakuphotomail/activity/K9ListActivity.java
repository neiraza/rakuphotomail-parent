package jp.co.fttx.rakuphotomail.activity;

import android.app.ListActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.ListView;
import android.os.Bundle;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.helper.DateFormatter;

public class K9ListActivity extends ListActivity {
    @Override
    public void onCreate(Bundle icicle) {
        RakuPhotoActivity.setLanguage(this, RakuPhotoMail.getRakuPhotoLanguage());
        setTheme(RakuPhotoMail.getRakuPhotoTheme());
        super.onCreate(icicle);
        setupFormats();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupFormats();
    }

    private java.text.DateFormat mDateFormat;
    private java.text.DateFormat mTimeFormat;

    private void setupFormats() {
        mDateFormat = DateFormatter.getDateFormat(this);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);   // 12/24 date format
    }

    public java.text.DateFormat getTimeFormat() {
        return mTimeFormat;
    }

    public java.text.DateFormat getDateFormat() {
        return mDateFormat;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Shortcuts that work no matter what is selected
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP: {
            final ListView listView = getListView();
            if (RakuPhotoMail.useVolumeKeysForListNavigationEnabled()) {
                int currentPosition = listView.getSelectedItemPosition();
                if (currentPosition == AdapterView.INVALID_POSITION || listView.isInTouchMode()) {
                    currentPosition = listView.getFirstVisiblePosition();
                }
                if (currentPosition > 0) {
                    listView.setSelection(currentPosition - 1);
                }
                return true;
            }
        }
        case KeyEvent.KEYCODE_VOLUME_DOWN: {
            final ListView listView = getListView();
            if (RakuPhotoMail.useVolumeKeysForListNavigationEnabled()) {
                int currentPosition = listView.getSelectedItemPosition();
                if (currentPosition == AdapterView.INVALID_POSITION || listView.isInTouchMode()) {
                    currentPosition = listView.getFirstVisiblePosition();
                }

                if (currentPosition < listView.getCount()) {
                    listView.setSelection(currentPosition + 1);
                }
                return true;
            }
        }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Swallow these events too to avoid the audible notification of a volume change
        if (RakuPhotoMail.useVolumeKeysForListNavigationEnabled()) {
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                if (RakuPhotoMail.DEBUG)
                    Log.v(RakuPhotoMail.LOG_TAG, "Swallowed key up.");
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
