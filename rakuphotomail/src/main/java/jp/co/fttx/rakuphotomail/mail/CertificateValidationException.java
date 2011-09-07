
package jp.co.fttx.rakuphotomail.mail;

public class CertificateValidationException extends MessagingException {
    public static final long serialVersionUID = -1;

    public CertificateValidationException(String message) {
        super(message);
    }

    public CertificateValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}