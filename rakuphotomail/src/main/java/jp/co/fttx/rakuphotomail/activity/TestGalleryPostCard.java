package jp.co.fttx.rakuphotomail.activity;

import android.os.Bundle;
import android.view.Window;
import jp.co.fttx.rakuphotomail.R;

/**
 * Created by IntelliJ IDEA.
 * User: tooru.oguri
 * Date: 12/02/06
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class TestGalleryPostCard extends RakuPhotoActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show_postcard1);
    }
}
