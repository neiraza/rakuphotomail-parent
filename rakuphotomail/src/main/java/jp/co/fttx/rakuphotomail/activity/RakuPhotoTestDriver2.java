package jp.co.fttx.rakuphotomail.activity;

import jp.co.fttx.rakuphotomail.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RakuPhotoTestDriver2 extends Activity {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.gallery_view_mail_detail);
		TextView mSubject = (TextView) findViewById(R.id.gallery_mail_subject);
		mSubject.setText("この画面はメール詳細表示を確認する為のモック画面です");
		TextView mDate = (TextView) findViewById(R.id.gallery_mail_date);
		mDate.setText("2011/09/30 13:00 午後");
		TextView mFromAddress = (TextView) findViewById(R.id.gallery_mail_from_address);
		mFromAddress.setText("小栗徹");
		TextView mToAddress = (TextView) findViewById(R.id.gallery_mail_to);
		mToAddress.setText("元アナウンサー菊間（来春から弁護士）");
		TextView mCcAddress = (TextView) findViewById(R.id.gallery_mail_cc);
		mCcAddress.setText("孫正義");
		TextView mContent = (TextView) findViewById(R.id.gallery_mail_content);
		mContent.setText("転落事故や謹慎処分を経て、2回目の挑戦で弁護士になりました。しかし、本件に関しましても他社事業に関わることですので、ノーコメントとさせて頂きますとの事でした。");
	}
}
