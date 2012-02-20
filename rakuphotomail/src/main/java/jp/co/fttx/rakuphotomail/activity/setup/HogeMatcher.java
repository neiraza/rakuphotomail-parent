package jp.co.fttx.rakuphotomail.activity.setup;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: tooru.oguri
 * Date: 12/02/20
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
public class HogeMatcher {
    public static final Pattern EMAIL_ADDRESS_PATTERN
            = Pattern.compile(
//            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
//                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static void main(String[] args) {
        boolean result = EMAIL_ADDRESS_PATTERN.matcher("-@abcd012345678901234567890123456789012345678901234567890123456789-.").matches();
        if (result) {
            System.out.println("Match!!!");
        } else {
            System.out.println("No Match!!!");
        }
    }
}
