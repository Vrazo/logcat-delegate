package tk.nathanf.logcat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Used to filter {@link LogCatMessage} instances in a {@link LogCatDelegate}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class LogCatMessageFilter {
    /**
     * Used with {@link LogCatMessageFilter} to specify which part of the message to apply the
     * filter to when using {@link #isValid(LogCatMessage)}.
     */
    public enum MessageSpan {
        /**
         * The entire message including timestamp, tag, priority, etc. The format for a full message
         * is the same as calling {@link LogCatMessage#getFormatted()} with no parameters.
         *
         * @see LogCatMessage#getFormatted()
         */
        @SuppressWarnings("unused")
        Full,

        /**
         * Only the body of the message.
         *
         * @see LogCatMessage#getMessage()
         */
        Message,

        /**
         * Only the tag of the message.
         *
         * @see LogCatMessage#getMessage()
         */
        Tag,
    }

    /**
     * If set to true, only messages that do NOT match this filter will be considered valid.
     */
    private boolean reverse;

    private boolean forcePriorityMessageSpan;

    /**
     * The pattern to compare messages to.
     */
    private Pattern pattern;

    /**
     * What part of the message to apply the filter to. Default value is {@link MessageSpan#Full}
     */
    private MessageSpan messageSpan = MessageSpan.Full;

    /**
     * Create a new message filter with the specified pattern.
     *
     * @param regexPattern the pattern
     * @throws PatternSyntaxException if the pattern is not valid regex
     */
    public LogCatMessageFilter(String regexPattern) throws PatternSyntaxException {
        this.pattern = Pattern.compile(regexPattern);
        this.reverse = false;
    }

    /**
     * Internal function for forcing the message priority in a sub-classed filter.
     *
     * @param value true to force
     */
    void setForcePriorityMessageSpan(boolean value) {
        forcePriorityMessageSpan = value;
    }

    /**
     * Sets whether or not to reverse this filter.
     *
     * When reversed, only messages that do NOT match this filter will be considered valid by the
     * {@link #isValid(LogCatMessage)} method.
     *
     * @param reverse true if you want the filter to be reversed
     */
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    /**
     * Set the portion of the message that this filter should apply to. The default value for this
     * configuration is {@link MessageSpan#Full}.
     *
     * @param messageSpan the message span
     */
    public void setMessageSpan(MessageSpan messageSpan) {
        this.messageSpan = messageSpan;
    }

    /**
     * Determine whether or not a message should be included in this filter.
     *
     * @param message the message
     * @return true if the message is within this filter
     */
    public boolean isValid(LogCatMessage message) {
        String input = message.getFormatted();
        if (forcePriorityMessageSpan) {
            input = message.getPriority().getCharacter();
        } else if (this.messageSpan == MessageSpan.Message) {
            input = message.getMessage();
        } else if (this.messageSpan == MessageSpan.Tag) {
            input = message.getTag();
        }

        Matcher matcher = this.pattern.matcher(input);
        boolean found = matcher.matches();
        return (this.reverse) != found;
    }
}
