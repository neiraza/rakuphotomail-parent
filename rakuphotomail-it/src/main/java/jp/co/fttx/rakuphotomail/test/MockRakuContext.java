package jp.co.fttx.rakuphotomail.test;

import android.content.Context;
import android.content.ContextWrapper;

// gerrit test 1
// gerrit test 2
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
