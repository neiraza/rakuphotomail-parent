package jp.co.fttx.rakuphotomail.activity;

import android.app.ExpandableListActivity;
import android.os.Bundle;

import jp.co.fttx.rakuphotomail.RakuPhotoMail;

/**
 * @see ExpandableListActivity
 */
public class K9ExpandableListActivity extends ExpandableListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(RakuPhotoMail.getRakuPhotoTheme());
        super.onCreate(savedInstanceState);
    }
}
