package jp.co.fttx.rakuphotomail.mail.transport;

import android.util.Log;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.*;
import jp.co.fttx.rakuphotomail.mail.Message.RecipientType;
import jp.co.fttx.rakuphotomail.mail.filter.Base64;
import jp.co.fttx.rakuphotomail.mail.filter.EOLConvertingOutputStream;
import jp.co.fttx.rakuphotomail.mail.filter.LineWrapOutputStream;
import jp.co.fttx.rakuphotomail.mail.filter.PeekableInputStream;
import jp.co.fttx.rakuphotomail.mail.filter.SmtpDataStuffing;
import jp.co.fttx.rakuphotomail.mail.internet.MimeUtility;
import jp.co.fttx.rakuphotomail.mail.store.TrustManagerFactory;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalMessage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import java.util.*;

public class SmtpTransport extends Transport {
    public static final int CONNECTION_SECURITY_NONE = 0;

    public static final int CONNECTION_SECURITY_TLS_OPTIONAL = 1;

    public static final int CONNECTION_SECURITY_TLS_REQUIRED = 2;

    public static final int CONNECTION_SECURITY_SSL_REQUIRED = 3;

    public static final int CONNECTION_SECURITY_SSL_OPTIONAL = 4;

    public static final String AUTH_PLAIN = "PLAIN";

    public static final String AUTH_CRAM_MD5 = "CRAM_MD5";

    public static final String AUTH_LOGIN = "LOGIN";

    public static final String AUTH_AUTOMATIC = "AUTOMATIC";

    String mHost;

    int mPort;

    String mUsername;

    String mPassword;

    String mAuthType;

    int mConnectionSecurity;

    boolean mSecure;

    Socket mSocket;

    PeekableInputStream mIn;

    OutputStream mOut;
    private boolean m8bitEncodingAllowed;

    private int mLargestAcceptableMessage;

    /**
     * smtp://user:password@server:port CONNECTION_SECURITY_NONE
     * smtp+tls://user:password@server:port CONNECTION_SECURITY_TLS_OPTIONAL
     * smtp+tls+://user:password@server:port CONNECTION_SECURITY_TLS_REQUIRED
     * smtp+ssl+://user:password@server:port CONNECTION_SECURITY_SSL_REQUIRED
     * smtp+ssl://user:password@server:port CONNECTION_SECURITY_SSL_OPTIONAL
     *
     * @param _uri
     */
    public SmtpTransport(String _uri) throws MessagingException {
        URI uri;
        try {
            uri = new URI(_uri);
        } catch (URISyntaxException use) {
            throw new MessagingException("Invalid SmtpTransport URI", use);
        }

        String scheme = uri.getScheme();
        if (scheme.equals("smtp")) {
            mConnectionSecurity = CONNECTION_SECURITY_NONE;
            mPort = 25;
        } else if (scheme.equals("smtp+tls")) {
            mConnectionSecurity = CONNECTION_SECURITY_TLS_OPTIONAL;
            mPort = 25;
        } else if (scheme.equals("smtp+tls+")) {
            mConnectionSecurity = CONNECTION_SECURITY_TLS_REQUIRED;
            mPort = 25;
        } else if (scheme.equals("smtp+ssl+")) {
            mConnectionSecurity = CONNECTION_SECURITY_SSL_REQUIRED;
            mPort = 465;
        } else if (scheme.equals("smtp+ssl")) {
            mConnectionSecurity = CONNECTION_SECURITY_SSL_OPTIONAL;
            mPort = 465;
        } else {
            throw new MessagingException("Unsupported protocol");
        }

        mHost = uri.getHost();

        if (uri.getPort() != -1) {
            mPort = uri.getPort();
        }

        if (uri.getUserInfo() != null) {
            try {
                String[] userInfoParts = uri.getUserInfo().split(":");
                mUsername = URLDecoder.decode(userInfoParts[0], "UTF-8");
                if (userInfoParts.length > 1) {
                    mPassword = URLDecoder.decode(userInfoParts[1], "UTF-8");
                }
                if (userInfoParts.length > 2) {
                    mAuthType = userInfoParts[2];
                }
            } catch (UnsupportedEncodingException enc) {
                // This shouldn't happen since the encoding is hardcoded to UTF-8
                Log.e(RakuPhotoMail.LOG_TAG, "Couldn't urldecode username or password.", enc);
            }
        }
    }

    @Override
    public void open() throws MessagingException {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(mHost);
            for (int i = 0; i < addresses.length; i++) {
                try {
                    SocketAddress socketAddress = new InetSocketAddress(addresses[i], mPort);
                    if (mConnectionSecurity == CONNECTION_SECURITY_SSL_REQUIRED ||
                            mConnectionSecurity == CONNECTION_SECURITY_SSL_OPTIONAL) {
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        boolean secure = mConnectionSecurity == CONNECTION_SECURITY_SSL_REQUIRED;
                        sslContext.init(null, new TrustManager[]{
                                TrustManagerFactory.get(mHost, secure)
                        }, new SecureRandom());
                        mSocket = sslContext.getSocketFactory().createSocket();
                        mSocket.connect(socketAddress, SOCKET_CONNECT_TIMEOUT);
                        mSecure = true;
                    } else {
                        mSocket = new Socket();
                        mSocket.connect(socketAddress, SOCKET_CONNECT_TIMEOUT);
                    }
                } catch (ConnectException e) {
                    if (i < (addresses.length - 1)) {
                        // there are still other addresses for that host to try
                        continue;
                    }
                    throw new MessagingException("Cannot connect to host", e);
                }
                break; // connection success
            }

            // RFC 1047
            mSocket.setSoTimeout(SOCKET_READ_TIMEOUT);

            mIn = new PeekableInputStream(new BufferedInputStream(mSocket.getInputStream(), 1024));
            mOut = mSocket.getOutputStream();

            // Eat the banner
            executeSimpleCommand(null);

            InetAddress localAddress = mSocket.getLocalAddress();
            String localHost = localAddress.getCanonicalHostName();
            String ipAddr = localAddress.getHostAddress();

            if (localHost.equals("") || localHost.equals(ipAddr) || localHost.contains("_")) {
                // We don't have a FQDN or the hostname contains invalid
                // characters (see issue 2143), so use IP address.
                if (!ipAddr.equals("")) {
                    if (localAddress instanceof Inet6Address) {
                        localHost = "[IPV6:" + ipAddr + "]";
                    } else {
                        localHost = "[" + ipAddr + "]";
                    }
                } else {
                    // If the IP address is no good, set a sane default (see issue 2750).
                    localHost = "android";
                }
            }

            List<String> results = executeSimpleCommand("EHLO " + localHost);

            m8bitEncodingAllowed = results.contains("8BITMIME");


            /*
            * using HELO on non STARTTLS connections because of AOL's mail
            * server. It won't let you use AUTH without EHLO.
            * We should really be paying more attention to the capabilities
            * and only attempting auth if it's available, and warning the user
            * if not.
            */
            if (mConnectionSecurity == CONNECTION_SECURITY_TLS_OPTIONAL
                    || mConnectionSecurity == CONNECTION_SECURITY_TLS_REQUIRED) {
                if (results.contains("STARTTLS")) {
                    executeSimpleCommand("STARTTLS");

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    boolean secure = mConnectionSecurity == CONNECTION_SECURITY_TLS_REQUIRED;
                    sslContext.init(null, new TrustManager[]{
                            TrustManagerFactory.get(mHost, secure)
                    }, new SecureRandom());
                    mSocket = sslContext.getSocketFactory().createSocket(mSocket, mHost, mPort,
                            true);
                    mIn = new PeekableInputStream(new BufferedInputStream(mSocket.getInputStream(),
                            1024));
                    mOut = mSocket.getOutputStream();
                    mSecure = true;
                    /*
                     * Now resend the EHLO. Required by RFC2487 Sec. 5.2, and more specifically,
                     * Exim.
                     */
                    results = executeSimpleCommand("EHLO " + localHost);
                } else if (mConnectionSecurity == CONNECTION_SECURITY_TLS_REQUIRED) {
                    throw new MessagingException("TLS not supported but required");
                }
            }

            boolean useAuthLogin = AUTH_LOGIN.equals(mAuthType);
            boolean useAuthPlain = AUTH_PLAIN.equals(mAuthType);
            boolean useAuthCramMD5 = AUTH_CRAM_MD5.equals(mAuthType);

            // Automatically choose best authentication method if none was explicitly selected
            boolean useAutomaticAuth = !(useAuthLogin || useAuthPlain || useAuthCramMD5);

            boolean authLoginSupported = false;
            boolean authPlainSupported = false;
            boolean authCramMD5Supported = false;
            for (String result : results) {

                if (result.matches(".*AUTH.*LOGIN.*$")) {
                    authLoginSupported = true;
                }
                if (result.matches(".*AUTH.*PLAIN.*$")) {
                    authPlainSupported = true;
                }
                if (result.matches(".*AUTH.*CRAM-MD5.*$")) {
                    authCramMD5Supported = true;
                }
                if (result.matches(".*SIZE \\d*$")) {
                    try {
                        mLargestAcceptableMessage = Integer.parseInt(result.substring(result.lastIndexOf(' ') + 1));
                    } catch (Exception e) {
                        if (RakuPhotoMail.DEBUG && RakuPhotoMail.DEBUG_PROTOCOL_SMTP) {
                            Log.d(RakuPhotoMail.LOG_TAG, "Tried to parse " + result + " and get an int out of the last word: " + e);
                        }
                    }
                }
            }

            if (mUsername != null && mUsername.length() > 0 &&
                    mPassword != null && mPassword.length() > 0) {
                if (useAuthCramMD5 || (useAutomaticAuth && authCramMD5Supported)) {
                    if (!authCramMD5Supported && RakuPhotoMail.DEBUG && RakuPhotoMail.DEBUG_PROTOCOL_SMTP) {
                        Log.d(RakuPhotoMail.LOG_TAG, "Using CRAM_MD5 as authentication method although the " +
                                "server didn't advertise support for it in EHLO response.");
                    }
                    saslAuthCramMD5(mUsername, mPassword);
                } else if (useAuthPlain || (useAutomaticAuth && authPlainSupported)) {
                    if (!authPlainSupported && RakuPhotoMail.DEBUG && RakuPhotoMail.DEBUG_PROTOCOL_SMTP) {
                        Log.d(RakuPhotoMail.LOG_TAG, "Using PLAIN as authentication method although the " +
                                "server didn't advertise support for it in EHLO response.");
                    }
                    try {
                        saslAuthPlain(mUsername, mPassword);
                    } catch (MessagingException ex) {
                        // PLAIN is a special case.  Historically, PLAIN has represented both PLAIN and LOGIN; only the
                        // protocol being advertised by the server would be used, with PLAIN taking precedence.  Instead
                        // of using only the requested protocol, we'll try PLAIN and then try LOGIN.
                        if (useAuthPlain && authLoginSupported) {
                            if (RakuPhotoMail.DEBUG && RakuPhotoMail.DEBUG_PROTOCOL_SMTP) {
                                Log.d(RakuPhotoMail.LOG_TAG, "Using legacy PLAIN authentication behavior and trying LOGIN.");
                            }
                            saslAuthLogin(mUsername, mPassword);
                        } else {
                            // If it was auto detected and failed, continue throwing the exception back up.
                            throw ex;
                        }
                    }
                } else if (useAuthLogin || (useAutomaticAuth && authLoginSupported)) {
                    if (!authPlainSupported && RakuPhotoMail.DEBUG && RakuPhotoMail.DEBUG_PROTOCOL_SMTP) {
                        Log.d(RakuPhotoMail.LOG_TAG, "Using LOGIN as authentication method although the " +
                                "server didn't advertise support for it in EHLO response.");
                    }
                    saslAuthLogin(mUsername, mPassword);
                } else {
                    throw new MessagingException("No valid authentication mechanism found.");
                }
            }
        } catch (SSLException e) {
            throw new CertificateValidationException(e.getMessage(), e);
        } catch (GeneralSecurityException gse) {
            throw new MessagingException(
                    "Unable to open connection to SMTP server due to security error.", gse);
        } catch (IOException ioe) {
            throw new MessagingException("Unable to open connection to SMTP server.", ioe);
        }
    }

    @Override
    public void sendMessage(Message message) throws MessagingException {
        ArrayList<Address> addresses = new ArrayList<Address>();
        {
            addresses.addAll(Arrays.asList(message.getRecipients(RecipientType.TO)));
            addresses.addAll(Arrays.asList(message.getRecipients(RecipientType.CC)));
            addresses.addAll(Arrays.asList(message.getRecipients(RecipientType.BCC)));
        }
        message.setRecipients(RecipientType.BCC, null);

        HashMap<String, ArrayList<String>> charsetAddressesMap =
                new HashMap<String, ArrayList<String>>();
        for (Address address : addresses) {
            String addressString = address.getAddress();
            String charset = MimeUtility.getCharsetFromAddress(addressString);
            ArrayList<String> addressesOfCharset = charsetAddressesMap.get(charset);
            if (addressesOfCharset == null) {
                addressesOfCharset = new ArrayList<String>();
                charsetAddressesMap.put(charset, addressesOfCharset);
            }
            addressesOfCharset.add(addressString);
        }

        for (Map.Entry<String, ArrayList<String>> charsetAddressesMapEntry :
                charsetAddressesMap.entrySet()) {
            String charset = charsetAddressesMapEntry.getKey();
            ArrayList<String> addressesOfCharset = charsetAddressesMapEntry.getValue();
            message.setCharset(charset);
            sendMessageTo(addressesOfCharset, message);
        }
    }

    private void sendMessageTo(ArrayList<String> addresses, Message message)
            throws MessagingException {
        boolean possibleSend = false;

        close();
        open();

        message.setEncoding(m8bitEncodingAllowed ? "8bit" : null);
        // If the message has attachments and our server has told us about a limit on
        // the size of messages, count the message's size before sending it
        if (mLargestAcceptableMessage > 0 && ((LocalMessage) message).hasAttachments()) {
            if (message.calculateSize() > mLargestAcceptableMessage) {
                MessagingException me = new MessagingException("Message too large for server");
                me.setPermanentFailure(possibleSend);
                throw me;
            }
        }

        Address[] from = message.getFrom();
        try {
            executeSimpleCommand("MAIL FROM:" + "<" + from[0].getAddress() + ">");
            for (String address : addresses) {
                executeSimpleCommand("RCPT TO:" + "<" + address + ">");
            }
            executeSimpleCommand("DATA");

            EOLConvertingOutputStream msgOut = new EOLConvertingOutputStream(
                    new SmtpDataStuffing(
                            new LineWrapOutputStream(
                                    new BufferedOutputStream(mOut, 1024),
                                    1000)));

            message.writeTo(msgOut);

            // We use BufferedOutputStream. So make sure to call flush() !
            msgOut.flush();

            possibleSend = true; // After the "\r\n." is attempted, we may have sent the message
            executeSimpleCommand("\r\n.");
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, e.getMessage());
            Log.e(RakuPhotoMail.LOG_TAG, Arrays.toString(e.getStackTrace()));
            MessagingException me = new MessagingException("Unable to send message", e);
            me.setPermanentFailure(possibleSend);
            throw me;
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
            executeSimpleCommand("QUIT");
        } catch (Exception e) {

        }
        try {
            mIn.close();
        } catch (Exception e) {

        }
        try {
            mOut.close();
        } catch (Exception e) {

        }
        try {
            mSocket.close();
        } catch (Exception e) {

        }
        mIn = null;
        mOut = null;
        mSocket = null;
    }

    private String readLine() throws IOException {
        StringBuffer sb = new StringBuffer();
        int d;
        while ((d = mIn.read()) != -1) {
            if (((char) d) == '\r') {
                continue;
            } else if (((char) d) == '\n') {
                break;
            } else {
                sb.append((char) d);
            }
        }
        String ret = sb.toString();
        if (RakuPhotoMail.DEBUG && RakuPhotoMail.DEBUG_PROTOCOL_SMTP)
            Log.d(RakuPhotoMail.LOG_TAG, "SMTP <<< " + ret);

        return ret;
    }

    private void writeLine(String s, boolean sensitive) throws IOException {
        if (RakuPhotoMail.DEBUG && RakuPhotoMail.DEBUG_PROTOCOL_SMTP) {
            final String commandToLog;
            if (sensitive && !RakuPhotoMail.DEBUG_SENSITIVE) {
                commandToLog = "SMTP >>> *sensitive*";
            } else {
                commandToLog = "SMTP >>> " + s;
            }
            Log.d(RakuPhotoMail.LOG_TAG, commandToLog);
        }

        byte[] data = s.concat("\r\n").getBytes();

        /*
         * Important: Send command + CRLF using just one write() call. Using
         * multiple calls will likely result in multiple TCP packets and some
         * SMTP servers misbehave if CR and LF arrive in separate pakets.
         * See issue 799.
         */
        mOut.write(data);
        mOut.flush();
    }

    private void checkLine(String line) throws MessagingException {
        if (line.length() < 1) {
            throw new MessagingException("SMTP response is 0 length");
        }
        char c = line.charAt(0);
        if ((c == '4') || (c == '5')) {
            throw new MessagingException(line);
        }
    }

    private List<String> executeSimpleCommand(String command) throws IOException, MessagingException {
        return executeSimpleCommand(command, false);
    }

    private List<String> executeSimpleCommand(String command, boolean sensitive)
            throws IOException, MessagingException {
        List<String> results = new ArrayList<String>();
        if (command != null) {
            writeLine(command, sensitive);
        }

        /*
         * Read lines as long as the length is 4 or larger, e.g. "220-banner text here".
         * Shorter lines are either errors of contain only a reply code. Those cases will
         * be handled by checkLine() below.
         */
        String line = readLine();
        while (line.length() >= 4) {
            if (line.length() > 4) {
                // Everything after the first four characters goes into the results array.
                results.add(line.substring(4));
            }

            if (line.charAt(3) != '-') {
                // If the fourth character isn't "-" this is the last line of the response.
                break;
            }
            line = readLine();
        }

        // Check if the reply code indicates an error.
        checkLine(line);

        return results;
    }


//    C: AUTH LOGIN
//    S: 334 VXNlcm5hbWU6
//    C: d2VsZG9u
//    S: 334 UGFzc3dvcmQ6
//    C: dzNsZDBu
//    S: 235 2.0.0 OK Authenticated
//
//    Lines 2-5 of the conversation contain base64-encoded information. The same conversation, with base64 strings decoded, reads:
//
//
//    C: AUTH LOGIN
//    S: 334 Username:
//    C: weldon
//    S: 334 Password:
//    C: w3ld0n
//    S: 235 2.0.0 OK Authenticated

    private void saslAuthLogin(String username, String password) throws MessagingException,
            AuthenticationFailedException, IOException {
        try {
            executeSimpleCommand("AUTH LOGIN");
            executeSimpleCommand(new String(Base64.encodeBase64(username.getBytes())), true);
            executeSimpleCommand(new String(Base64.encodeBase64(password.getBytes())), true);
        } catch (MessagingException me) {
            if (me.getMessage().length() > 1 && me.getMessage().charAt(1) == '3') {
                throw new AuthenticationFailedException("AUTH LOGIN failed (" + me.getMessage()
                        + ")");
            }
            throw me;
        }
    }

    private void saslAuthPlain(String username, String password) throws MessagingException,
            AuthenticationFailedException, IOException {

        byte[] data = ("\000" + username + "\000" + password).getBytes();
        data = new Base64().encode(data);
        try {
            executeSimpleCommand("AUTH PLAIN " + new String(data), true);
        } catch (MessagingException me) {
            if (me.getMessage().length() > 1 && me.getMessage().charAt(1) == '3') {
                throw new AuthenticationFailedException("AUTH PLAIN failed (" + me.getMessage()
                        + ")");
            }
            throw me;
        }
    }

    private void saslAuthCramMD5(String username, String password) throws MessagingException,
            AuthenticationFailedException, IOException {

        List<String> respList = executeSimpleCommand("AUTH CRAM-MD5");
        if (respList.size() != 1) {
            throw new AuthenticationFailedException("Unable to negotiate CRAM-MD5");
        }

        String b64Nonce = respList.get(0);
        String b64CRAMString = Authentication.computeCramMd5(mUsername, mPassword, b64Nonce);

        try {
            executeSimpleCommand(b64CRAMString, true);
        } catch (MessagingException me) {
            throw new AuthenticationFailedException("Unable to negotiate MD5 CRAM");
        }
    }
}
