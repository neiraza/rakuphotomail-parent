package jp.co.fttx.rakuphotomail.mail.filter;

import android.util.Config;
import android.util.Log;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StatusOutputStream extends FilterOutputStream {
    private long mCount = 0;

    public StatusOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int oneByte) throws IOException {
        super.write(oneByte);
        mCount++;
        if (Config.LOGV) {
            if (mCount % 1024 == 0) {
                Log.v(RakuPhotoMail.LOG_TAG, "# " + mCount);
            }
        }
    }
}
