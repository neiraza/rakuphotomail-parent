
package jp.co.fttx.rakuphotomail.mail;

public class MessagingException extends Exception {
    public static final long serialVersionUID = -1;

    boolean permanentFailure = false;

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, boolean perm) {
        super(message);
        permanentFailure = perm;
    }

    public MessagingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MessagingException(String message, boolean perm, Throwable throwable) {
        super(message, throwable);
        permanentFailure = perm;
    }

    public boolean isPermanentFailure() {
        return permanentFailure;
    }

    public void setPermanentFailure(boolean permanentFailure) {
        this.permanentFailure = permanentFailure;
    }


}
