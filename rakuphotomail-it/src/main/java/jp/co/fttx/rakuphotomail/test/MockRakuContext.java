package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import android.content.Context;
import android.content.ContextWrapper;

public class MockRakuContext extends ContextWrapper {

	Context context;
	
	public MockRakuContext(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public Context getApplicationContext() {
		return this.context.getApplicationContext();
	}
	
	@Override
	public String getPackageName(){
		return "jp.co.fttx.rakuphotomail";
	}

}
