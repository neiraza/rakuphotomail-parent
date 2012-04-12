package jp.co.fttx.rakuphotomail.test;

import android.test.AndroidTestCase;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

/**
 * @author tooru.oguri
 */
public class RakuPhotoStringUtilsTest extends AndroidTestCase {

    public RakuPhotoStringUtilsTest() {
        super();
    }

    public void testNgOver1() {
        String result = RakuPhotoStringUtils.limitMessage("あいうえおかきくけこさしすせそたちつてとな", 20);
        assertEquals("あいうえおかきくけこさしすせそたちつてと...", result);
    }

    public void testNgOver2() {
        String result = RakuPhotoStringUtils.limitMessage("あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほ", 20);
        assertEquals("あいうえおかきくけこさしすせそたちつてと...", result);
    }

    public void testOk1() {
        String result = RakuPhotoStringUtils.limitMessage("あいうえおかきくけこさしすせそたちつてと", 20);
        assertEquals("あいうえおかきくけこさしすせそたちつてと", result);
    }

    public void testOk2() {
        String result = RakuPhotoStringUtils.limitMessage("あいうえお", 20);
        assertEquals("あいうえお", result);
    }

    public void testOk3() {
        String result = RakuPhotoStringUtils.limitMessage("", 20);
        assertEquals("", result);
    }
}
