package jp.co.fttx.rakuphotomail.activity;

import java.io.Serializable;

/**
 * <p>Represents an HTML document with an insertion point for placing a reply. The quoted
 * document may have been modified to make it suitable for insertion. The modified quoted
 * document should be used in place of the original document.</p>
 *
 * <p>Changes to the user-generated inserted content should be done with {@link
 * #setUserContent(String)}.</p>
 *
 */
class InsertableHtmlContent implements Serializable {
    private static final long serialVersionUID = 2397327034L;
    // Default to a headerInsertionPoint at the beginning of the message.
    private int headerInsertionPoint = 0;
    private int footerInsertionPoint = 0;
    // Quoted message, if any.  headerInsertionPoint refers to a position in this string.
    private StringBuilder quotedContent = new StringBuilder();
    // User content (typically their reply or comments on a forward)
    private StringBuilder userContent = new StringBuilder();
    // Where to insert the content.  Default to top posting.
    private InsertionLocation insertionLocation = InsertionLocation.BEFORE_QUOTE;

    /**
     * Defines where user content should be inserted, either before or after quoted content.
     */
    public enum InsertionLocation {
        BEFORE_QUOTE, AFTER_QUOTE
    }

    public void setHeaderInsertionPoint(int headerInsertionPoint) {
        this.headerInsertionPoint = headerInsertionPoint;
    }

    public void setFooterInsertionPoint(int footerInsertionPoint) {
        this.footerInsertionPoint = footerInsertionPoint;
    }

    /**
     * Get the quoted content.
     * @return Quoted content.
     */
    public String getQuotedContent() {
        return quotedContent.toString();
    }

    /**
     * Set the quoted content.  The insertion point should be set against this content.
     * @param content
     */
    public void setQuotedContent(StringBuilder content) {
        this.quotedContent = content;
    }

    /**
     * <p>Insert something into the quoted content header. This is typically used for inserting
     * reply/forward headers into the quoted content rather than inserting the user-generated reply
     * content.</p>
     *
     * <p>Subsequent calls to {@link #insertIntoQuotedHeader(String)} will <b>prepend</b> text onto any
     * existing header and quoted content.</p>
     * @param content Content to add.
     */
    public void insertIntoQuotedHeader(final String content) {
        quotedContent.insert(headerInsertionPoint, content);
        // Update the location of the footer insertion point.
        footerInsertionPoint += content.length();
    }

    /**
     * <p>Insert something into the quoted content footer. This is typically used for inserting closing
     * tags of reply/forward headers rather than inserting the user-generated reply content.</p>
     *
     * <p>Subsequent calls to {@link #insertIntoQuotedFooter(String)} will <b>append</b> text onto any
     * existing footer and quoted content.</p>
     * @param content Content to add.
     */
    public void insertIntoQuotedFooter(final String content) {
        quotedContent.insert(footerInsertionPoint, content);
        // Update the location of the footer insertion point to the end of the inserted content.
        footerInsertionPoint += content.length();
    }

    /**
     * Remove all quoted content.
     */
    public void clearQuotedContent() {
        quotedContent.setLength(0);
        footerInsertionPoint = 0;
        headerInsertionPoint = 0;
    }

    /**
     * Set the inserted content to the specified content. Replaces anything currently in the
     * inserted content buffer.
     * @param content
     */
    public void setUserContent(final String content) {
        userContent = new StringBuilder(content);
    }

    /**
     * Configure where user content should be inserted, either before or after the quoted content.
     * @param insertionLocation Where to insert user content.
     */
    public void setInsertionLocation(final InsertionLocation insertionLocation) {
        this.insertionLocation = insertionLocation;
    }

    /**
     * Fetch the insertion point based upon the quote style.
     * @return Insertion point
     */
    public int getInsertionPoint() {
        if (insertionLocation == InsertionLocation.BEFORE_QUOTE) {
            return headerInsertionPoint;
        } else {
            return footerInsertionPoint;
        }
    }

    /**
     * Build the composed string with the inserted and original content.
     * @return Composed string.
     */
    @Override
    public String toString() {
        final int insertionPoint = getInsertionPoint();
        // Inserting and deleting was twice as fast as instantiating a new StringBuilder and
        // using substring() to build the new pieces.
        String result = quotedContent.insert(insertionPoint, userContent.toString()).toString();
        quotedContent.delete(insertionPoint, insertionPoint + userContent.length());
        return result;
    }

    /**
     * Return debugging information for this container.
     * @return Debug string.
     */
    public String toDebugString() {
        return "InsertableHtmlContent{" +
               "headerInsertionPoint=" + headerInsertionPoint +
               ", footerInsertionPoint=" + footerInsertionPoint +
               ", insertionLocation=" + insertionLocation +
               ", quotedContent=" + quotedContent +
               ", userContent=" + userContent +
               ", compiledResult=" + toString() +
               '}';
    }
}
