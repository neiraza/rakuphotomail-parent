package jp.co.fttx.rakuphotomail.mail.store;

import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.filter.FixedLengthInputStream;
import jp.co.fttx.rakuphotomail.mail.filter.PeekableInputStream;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImapResponseParser {
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss Z", Locale.US);
    private static final SimpleDateFormat badDateTimeFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US);
    private static final SimpleDateFormat badDateTimeFormat2 = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);
    private static final SimpleDateFormat badDateTimeFormat3 = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US);

    private PeekableInputStream mIn;
    private ImapResponse mResponse;
    private Exception mException;

    public ImapResponseParser(PeekableInputStream in) {
        this.mIn = in;
    }

    public ImapResponse readResponse() throws IOException {
        return readResponse(null);
    }

    /**
     * Reads the next response available on the stream and returns an
     * ImapResponse object that represents it.
     */
    public ImapResponse readResponse(IImapResponseCallback callback) throws IOException {
        try {
            ImapResponse response = new ImapResponse();
            mResponse = response;
            mResponse.mCallback = callback;

            int ch = mIn.peek();
            if (ch == '*') {
                parseUntaggedResponse();
                readTokens(response);
            } else if (ch == '+') {
                response.mCommandContinuationRequested =
                        parseCommandContinuationRequest();
                readTokens(response);
            } else {
                response.mTag = parseTaggedResponse();
                readTokens(response);
            }

            if (mException != null) {
                throw new RuntimeException("readResponse(): Exception in callback method", mException);
            }

            return response;
        } catch (RakuRakuException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (null != mResponse) {
                mResponse.mCallback = null;
                mResponse = null;
            }
            mException = null;
        }
        return null;
    }

    private void readTokens(ImapResponse response) throws IOException {
        response.clear();
        Object token;
        while ((token = readToken(response)) != null) {
            if (!(token instanceof ImapList)) {
                response.add(token);
            }

            /*
             * that can contain resp-text tokens. If found, hand over to a special
             * method that parses a resp-text token. There's no need to use
             * readToken()/parseToken() on that data.
             *
             * See RFC 3501, Section 9 Formal Syntax (resp-text)
             */
        }
        response.mCompleted = true;
    }

    /**
     * Reads the next token of the response. The token can be one of: String -
     * for NIL, QUOTED, NUMBER, ATOM. Object - for LITERAL.
     * ImapList - for PARENTHESIZED LIST. Can contain any of the above
     * elements including List.
     *
     * @return The next token in the response or null if there are no more
     *         tokens.
     */
    private Object readToken(ImapResponse response) throws IOException {
        while (true) {
            Object token = parseToken(response);
            if (token == null || !(token.equals(")") || token.equals("]"))) {
                return token;
            }
        }
    }

    private Object parseToken(ImapList parent) throws IOException {
        while (true) {
            int ch = mIn.peek();
            if (ch == '(') {
                return parseList(parent);
            } else if (ch == '[') {
                return parseSequence(parent);
            } else if (ch == ')') {
                expect(')');
                return ")";
            } else if (ch == ']') {
                expect(']');
                return "]";
            } else if (ch == '"') {
                return parseQuoted();
            } else if (ch == '{') {
                return parseLiteral();
            } else if (ch == ' ') {
                expect(' ');
            } else if (ch == '\r') {
                expect('\r');
                expect('\n');
                return null;
            } else if (ch == '\n') {
                expect('\n');
                return null;
            } else if (ch == '\t') {
                expect('\t');
            } else {
                return parseAtom();
            }
        }
    }

    private boolean parseCommandContinuationRequest() throws IOException {
        expect('+');
        return true;
    }

    // * OK [UIDNEXT 175] Predicted next UID
    private void parseUntaggedResponse() throws IOException {
        expect('*');
        expect(' ');
    }

    // 3 OK [READ-WRITE] Select completed.
    private String parseTaggedResponse() throws IOException, RakuRakuException {
        String tag = readStringUntil(' ');
        return tag;
    }

    private ImapList parseList(ImapList parent) throws IOException {
        expect('(');
        ImapList list = new ImapList();
        parent.add(list);
        Object token;
        while (true) {
            token = parseToken(list);
            if (token == null) {
                return null;
            } else if (token.equals(")")) {
                break;
            } else if (token instanceof ImapList) {
                // Do nothing
            } else {
                list.add(token);
            }
        }
        return list;
    }

    private ImapList parseSequence(ImapList parent) throws IOException {
        expect('[');
        ImapList list = new ImapList();
        parent.add(list);
        Object token;
        while (true) {
            token = parseToken(list);
            if (token == null) {
                return null;
            } else if (token.equals("]")) {
                break;
            } else if (token instanceof ImapList) {
                // Do nothing
            } else {
                list.add(token);
            }
        }
        return list;
    }

    private String parseAtom() throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while (true) {
            ch = mIn.peek();
            if (ch == -1) {
                throw new IOException("parseAtom(): end of stream reached");
            } else if (ch == '(' || ch == ')' || ch == '{' || ch == ' ' ||
                    ch == '[' || ch == ']' ||
                    ch == '"' || (ch >= 0x00 && ch <= 0x1f) || ch == 0x7f) {
                if (sb.length() == 0) {
                    throw new IOException(String.format("parseAtom(): (%04x %c)", ch, ch));
                }
                return sb.toString();
            } else {
                sb.append((char) mIn.read());
            }
        }
    }

    /**
     * A "{" has been read. Read the rest of the size string, the space and then
     * notify the callback with an InputStream.
     */
    private Object parseLiteral() throws IOException {
        expect('{');
        int size = Integer.parseInt(readStringUntil('}'));
        expect('\r');
        expect('\n');

        if (size == 0) {
            return "";
        }

        if (mResponse.mCallback != null) {
            FixedLengthInputStream fixed = new FixedLengthInputStream(mIn, size);

            Object result = null;
            try {
                result = mResponse.mCallback.foundLiteral(mResponse, fixed);
            } catch (IOException e) {
                // Pass IOExceptions through
                throw e;
            } catch (Exception e) {
                // Catch everything else and save it for later.
                mException = e;
                //Log.e(K9.LOG_TAG, "parseLiteral(): Exception in callback method", e);
            }

            // Check if only some of the literal data was read
            int available = fixed.available();
            if ((available > 0) && (available != size)) {
                // If so, skip the rest
                while (fixed.available() > 0) {
                    fixed.skip(fixed.available());
                }
            }

            if (result != null) {
                return result;
            }
        }

        byte[] data = new byte[size];
        int read = 0;
        while (read != size) {
            int count = mIn.read(data, read, size - read);
            if (count == -1) {
                throw new IOException("parseLiteral(): end of stream reached");
            }
            read += count;
        }

        return new String(data, "US-ASCII");
    }

    private String parseQuoted() throws IOException {
        expect('"');

        StringBuffer sb = new StringBuffer();
        int ch;
        boolean escape = false;
        while ((ch = mIn.read()) != -1) {
            if (!escape && (ch == '\\')) {
                // Found the escape character
                escape = true;
            } else if (!escape && (ch == '"')) {
                return sb.toString();
            } else {
                sb.append((char) ch);
                escape = false;
            }
        }
        throw new IOException("parseQuoted(): end of stream reached");
    }

    private String readStringUntil(char end) throws IOException {
        StringBuffer sb = new StringBuffer();
        int ch;
        while ((ch = mIn.read()) != -1) {
            if (ch == end) {
                return sb.toString();
            } else {
                sb.append((char) ch);
            }
        }
        throw new IOException("readStringUntil(): end of stream reached");
    }

    private int expect(char ch) throws IOException {
        int d;
        if ((d = mIn.read()) != ch) {
            throw new IOException(String.format("Expected %04x (%c) but got %04x (%c)", (int) ch,
                    ch, d, (char) d));
        }
        return d;
    }

    /**
     * Represents an IMAP list response and is also the base class for the
     * ImapResponse.
     */
    public class ImapList extends ArrayList<Object> {
        private static final long serialVersionUID = -4067248341419617583L;

        public ImapList getList(int index) {
            return (ImapList) get(index);
        }

        public Object getObject(int index) {
            return get(index);
        }

        public String getString(int index) {
            return (String) get(index);
        }

        public InputStream getLiteral(int index) {
            return (InputStream) get(index);
        }

        public int getNumber(int index) {
            return Integer.parseInt(getString(index));
        }

        public Date getDate(int index) throws MessagingException {
            return getDate(getString(index));
        }

        public Date getKeyedDate(Object key) throws MessagingException {
            return getDate(getKeyedString(key));
        }

        private Date getDate(String value) throws MessagingException {
            try {
                if (value == null) {
                    return null;
                }
                return parseDate(value);
            } catch (ParseException pe) {
                throw new MessagingException("Unable to parse IMAP datetime '" + value + "' ", pe);
            }
        }


        public Object getKeyedValue(Object key) {
            for (int i = 0, count = size(); i < count; i++) {
                if (equalsIgnoreCase(get(i), key)) {
                    return get(i + 1);
                }
            }
            return null;
        }

        public ImapList getKeyedList(Object key) {
            return (ImapList) getKeyedValue(key);
        }

        public String getKeyedString(Object key) {
            return (String) getKeyedValue(key);
        }

        public InputStream getKeyedLiteral(Object key) {
            return (InputStream) getKeyedValue(key);
        }

        public int getKeyedNumber(Object key) {
            return Integer.parseInt(getKeyedString(key));
        }

        public boolean containsKey(Object key) {
            if (key == null) {
                return false;
            }

            for (int i = 0, count = size(); i < count; i++) {
                if (equalsIgnoreCase(key, get(i))) {
                    return true;
                }
            }
            return false;
        }

        public int getKeyIndex(Object key) {
            for (int i = 0, count = size(); i < count; i++) {
                if (equalsIgnoreCase(key, get(i))) {
                    return i;
                }
            }

            throw new IllegalArgumentException("getKeyIndex() only works for keys that are in the collection.");
        }

        private Date parseDate(String value) throws ParseException {
            try {
                synchronized (mDateTimeFormat) {
                    return mDateTimeFormat.parse(value);
                }
            } catch (Exception e) {
                try {
                    synchronized (badDateTimeFormat) {
                        return badDateTimeFormat.parse(value);
                    }
                } catch (Exception e2) {
                    try {
                        synchronized (badDateTimeFormat2) {
                            return badDateTimeFormat2.parse(value);
                        }
                    } catch (Exception e3) {
                        synchronized (badDateTimeFormat3) {
                            return badDateTimeFormat3.parse(value);
                        }
                    }
                }
            }
        }
    }

    /**
     * Represents a single response from the IMAP server. Tagged responses will
     * have a non-null tag. Untagged responses will have a null tag. The object
     * will contain all of the available tokens at the time the response is
     * received. In general, it will either contain all of the tokens of the
     * response or all of the tokens up until the first LITERAL. If the object
     * does not contain the entire response the caller must call more() to
     * continue reading the response until more returns false.
     */
    public class ImapResponse extends ImapList {
        /**
         *
         */
        private static final long serialVersionUID = 6886458551615975669L;
        private boolean mCompleted;
        private IImapResponseCallback mCallback;

        boolean mCommandContinuationRequested;
        String mTag;

        public boolean more() throws IOException {
            if (mCompleted) {
                return false;
            }
            readTokens(this);
            return true;
        }

        public String getAlertText() {
            if (size() > 1 && equalsIgnoreCase("[ALERT]", get(1))) {
                StringBuffer sb = new StringBuffer();
                for (int i = 2, count = size(); i < count; i++) {
                    sb.append(get(i).toString());
                    sb.append(' ');
                }
                return sb.toString();
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return "#" + (mCommandContinuationRequested ? "+" : mTag) + "# " + super.toString();
        }
    }

    public static boolean equalsIgnoreCase(Object o1, Object o2) {
        if (o1 != null && o2 != null && o1 instanceof String && o2 instanceof String) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.equalsIgnoreCase(s2);
        } else if (o1 != null) {
            return o1.equals(o2);
        } else if (o2 != null) {
            return o2.equals(o1);
        } else {
            // Both o1 and o2 are null
            return true;
        }
    }

    public interface IImapResponseCallback {
        /**
         * Callback method that is called by the parser when a literal string
         * is found in an IMAP response.
         *
         * @param response ImapResponse object with the fields that have been
         *                 parsed up until now (excluding the literal string).
         * @param literal  FixedLengthInputStream that can be used to access
         *                 the literal string.
         * @return an Object that will be put in the ImapResponse object at the
         *         place of the literal string.
         * @throws IOException passed-through if thrown by FixedLengthInputStream
         * @throws Exception   if something goes wrong. Parsing will be resumed
         *                     and the exception will be thrown after the
         *                     complete IMAP response has been parsed.
         */
        public Object foundLiteral(ImapResponse response, FixedLengthInputStream literal)
                throws IOException, Exception;
    }
}
