package jp.co.fttx.rakuphotomail.test;

import android.test.ActivityInstrumentationTestCase2;
import jp.co.fttx.rakuphotomail.activity.Accounts;

public class AccountsTest extends
        ActivityInstrumentationTestCase2<Accounts> {

    public AccountsTest() {
        super("jp.co.fttx.rakuphotomail.activity", Accounts.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testObjectNotNull() {
        Accounts account = getActivity();
        assertNotNull(account);
    }

}
