package jp.co.fttx.rakuphotomail.test;

import jp.co.fttx.rakuphotomail.activity.Accounts;
import android.test.ActivityUnitTestCase;

public class AccountsTest extends
		ActivityUnitTestCase<Accounts> {

	public AccountsTest() {
		super(Accounts.class);
	}

//	private static final String EXTRA_ACCOUNT = "account";
//	private static final String EXTRA_FOLDER = "folder";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testHoge() {
		Accounts account = getActivity();
		assertNull(account);
	}

}
