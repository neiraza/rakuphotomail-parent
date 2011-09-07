package jp.co.fttx.rakuphotomail.rakuraku.util;

/**
 * 
 * @author tooru.oguri
 */
public class RakuPhotoStringUtil {

	/**
	 * <p>
	 * メールアドレスリスト分割.
	 * </p>
	 * カンマ毎に区切って送られてきたメールアドレスのリストを、<br>
	 * メールアドレス毎に分割する。<br>
	 * 
	 * @author tooru.oguri
	 * @param mailAdressStr
	 * @return String[] 分割したメールアドレス情報
	 */
	public static String[] splitMailAdressList(String mailAdressStr) {
		if (mailAdressStr != null) {
			String[] mailAddressList = mailAdressStr.split(",");
			if (mailAddressList != null) {
				return mailAddressList;
			}
		}
		return null;
	}

	/**
	 * <p>
	 * メールアドレス情報分割.
	 * </p>
	 * メールアドレス情報を名前とメールアドレスに分割する<br>
	 * 名前が存在しない場合のみ、メールアドレスを返却する。
	 * 
	 * @author tooru.oguri
	 * @param mailAddressList
	 * @return String メールアドレスの名前若しくはメールアドレス
	 */
	public static String splitMailAddress(String mailAddress) {
		String[] mailToArr = mailAddress.split(";");
		if (mailToArr == null || mailToArr.length == 0) {
			return "不明";
		} else if (mailToArr.length == 1) {
			return mailToArr[0];
		} else {
			return mailToArr[1];
		}
	}

	/**
	 * <p>
	 * 表示用メールアドレス取得.
	 * </p>
	 * メールアドレス情報を名前とメールアドレスに分割しながら、<br>
	 * 複数件のメールアドレス情報を連結し、返却する。
	 * 
	 * @author tooru.oguri
	 * @param mailAddressList
	 * @return StringBuffer 連結したメールアドレスの名前若しくはメールアドレス
	 */
	public static StringBuffer getMailAddress(String[] mailAddressList) {
		StringBuffer mailAddressInfo = new StringBuffer();
		for (String mailAddress : mailAddressList) {
			mailAddressInfo.append(RakuPhotoStringUtil.splitMailAddress(mailAddress));
			mailAddressInfo.append(",");
		}
		mailAddressInfo.deleteCharAt(mailAddressInfo.lastIndexOf(","));
		return mailAddressInfo;
	}

	/**
	 * <p>
	 * 表示用メールアドレス情報取得.
	 * </p>
	 * メールアドレスを下記の表記で連結し、返却する。<br>
	 * ほげ<hoge@huga.com><br>
	 * 但し、名前が存在しない場合はメールアドレスのみを返却する
	 * 
	 * @author tooru.oguri
	 * @param mailAddress
	 * @return String メールアドレス情報（or メールアドレス）
	 */
	public static String getMailAddressInfo(String mailAddress) {
		String[] mailAddressInfo = mailAddress.split(";");
		if (mailAddressInfo != null && mailAddressInfo.length == 2) {
			return mailAddressInfo[1] + "<" + mailAddressInfo[0] + ">";
		} else if (mailAddressInfo.length == 1) {
			return mailAddressInfo[0];
		} else {
			return "表示できません";
		}
	}

	/**
	 * <p>
	 * 表示用メールアドレス情報リスト取得.
	 * </p>
	 * メールアドレス情報リストを各メールアドレス情報毎に改行区切りで返却する。
	 * 
	 * @author tooru.oguri
	 * @param mailAddressList
	 * @return
	 */
	public static StringBuffer getMailAddressInfoList(String mailAddressList) {
		String[] buff = mailAddressList.split(",");
		StringBuffer mailAddressInfo = new StringBuffer();
		for (String mailAddress : buff) {
			mailAddressInfo.append(getMailAddressInfo(mailAddress));
			mailAddressInfo.append(System.getProperty("line.separator"));
		}
		mailAddressInfo.deleteCharAt(mailAddressInfo.lastIndexOf(System.getProperty("line.separator")));
		return mailAddressInfo;
	}
}
