/**
 * 
 */
package jp.co.fttx.rakuphotomail.rakuraku.exception;

/**
 * @author tooru.oguri
 *
 */
public class RakuRakuException extends Exception {
    
    /**
     * serial
     */
    static final long serialVersionUID = 1L;

    public RakuRakuException(String message, Throwable cause) {
        super(message, cause);
    }

    public RakuRakuException(String message) {
        super(message);
    }

    public RakuRakuException(Throwable cause) {
        super(cause);
    }

}
