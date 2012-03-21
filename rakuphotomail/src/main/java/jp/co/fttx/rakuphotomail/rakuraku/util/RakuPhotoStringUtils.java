package jp.co.fttx.rakuphotomail.rakuraku.util;

import android.util.Log;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;

/**
 * @author tooru.oguri
 */
public class RakuPhotoStringUtils {

    /**
     * Comma separated.
     *
     * @param str String
     * @return String[]
     * @author tooru.oguri
     */
    public static String[] splitComma(String str) {
        return str.split(",");
    }

    /**
     * <p>
     * messagesテーブル.flag分割.
     * </p>
     * カンマ毎に区切られたflag情報を項目毎に分割する。<br>
     *
     * @param flag messages.flag
     * @return String[] 分割したflag情報
     * @author tooru.oguri
     */
    public static String[] splitFlags(String flag) {
        if (flag != null) {
            String[] flagList = splitComma(flag);
            if (flagList != null) {
                return flagList;
            }
        }
        return null;
    }

    //TODO 140 limit
    public static String limitMessage(String str, int limit) {
        if (str.length() <= limit) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str.substring(0, limit));
            sb.append("...");
            return sb.toString();
        }

    }

    public static boolean isNotBlank(String... params) {
        for (String param : params) {
            if (null == param) {
                Log.w(RakuPhotoMail.LOG_TAG, "RakuPhotoStringUtils#isNotBlank paramsの中にnullがいたお");
                return false;
            }
            if ("".equals(param)) {
                Log.w(RakuPhotoMail.LOG_TAG, "RakuPhotoStringUtils#isNotBlank paramsの中にnullがいたお");
                return false;
            }
        }
        return true;
    }
}
