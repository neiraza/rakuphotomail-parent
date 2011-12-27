package jp.co.fttx.rakuphotomail.rakuraku.util;

/**
*
* @author tooru.oguri
*/
public class RakuPhotoStringUtils {

	/**
	 * Comma separated.
	 *
	 * @author tooru.oguri
	 * @param str String
	 * @return String[]
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
	 * @author tooru.oguri
	 * @param flag messages.flag
	 * @return String[] 分割したflag情報
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
}
