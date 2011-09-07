
package jp.co.fttx.rakuphotomail;

import jp.co.fttx.rakuphotomail.mail.Flag;

public interface SearchSpecification {

    public Flag[] getRequiredFlags();

    public Flag[] getForbiddenFlags();

    public boolean isIntegrate();

    public String getQuery();

    public String[] getAccountUuids();

    public String[] getFolderNames();
}