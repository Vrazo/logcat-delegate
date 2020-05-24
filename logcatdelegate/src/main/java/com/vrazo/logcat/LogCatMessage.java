package com.vrazo.logcat;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a message received from LogCat.
 */
@SuppressWarnings("unused")
public final class LogCatMessage {
    private Date loggedAt;
    private LogCatPriority priority;
    private int pid;
    private int tid;
    private String tag;
    private String message;
    private String raw;

    /**
     * Block access to instantiation.
     */
    private LogCatMessage() {}

    /**
     * Retrieve the formatted version of this message. By default, this will use the format of:
     *
     *     %de %p %r %vc %t: %m
     *
     * See {@link #getFormatted(String)} for more information on format specifiers.
     *
     * @return The formatted message.
     */
    public final String getFormatted() {
        return getFormatted("%de %p %r %vc %t: %m");
    }

    /**
     * Retrieve the formatted version of this message based on the provided format.
     *
     * <b>Valid format specifiers:</b>
     * <ul>
     *     <li><pre>%d</pre> - Timestamp using RFC3339Nano
     *     (see {@link #getFormatted(String, String)} for information on how to apply a custom
     *     date format.</li>
     *     <li><pre>%de</pre> - Timestamp as epoch time (ss.SSS)</li>
     *     <li><pre>%v</pre> - Priority name</li>
     *     <li><pre>%vi</pre> - Priority numeric code</li>
     *     <li><pre>%vc</pre> - Priority single letter</li>
     *     <li><pre>%p</pre> - Process ID</li>
     *     <li><pre>%r</pre> - Thread ID</li>
     *     <li><pre>%t</pre> - Tag</li>
     *     <li><pre>%m</pre> - Message</li>
     * </ul>
     *
     * @param format The format for the message.
     *
     * @return       The formatted message.
     */
    public final String getFormatted(String format) {
        return getFormatted(format, "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    /**
     * Retrieve the formatted version of this message based on the provided format and date format.
     *
     * Any <pre>%d</pre> format specifier will be replaced with the date using the format specified
     * in dateFormat.
     *
     * @see #getFormatted(String) for information on format specifiers.
     * @see SimpleDateFormat for information on DATE format specifiers.
     *
     * @param format     The format for the message.
     * @param dateFormat The format for the date.
     *
     * @return           The formatted message.
     */
    @SuppressWarnings("WeakerAccess")
    public final String getFormatted(String format, String dateFormat) {
        DecimalFormat decimalFormat = new DecimalFormat("#.000");
        format = format.replace("%de", decimalFormat.format(
            (double) this.loggedAt.getTime() / 1000D)
        );
        format = format.replace("%d", new SimpleDateFormat(
            dateFormat, Locale.US).format(this.loggedAt)
        );
        format = format.replace("%vi", Integer.toString(priority.getNumeric()));
        format = format.replace("%vc", priority.getCharacter());
        format = format.replace("%v", priority.getName());
        format = format.replace("%p", Integer.toString(pid));
        format = format.replace("%r", Integer.toString(tid));
        format = format.replace("%t", tag);
        format = format.replace("%m", message);
        return format;
    }

    /**
     * Retrieve the {@link Date} at which this message was logged.
     *
     * @return The Date.
     */
    @SuppressWarnings("WeakerAccess")
    public final Date getLoggedAt() {
        return loggedAt;
    }

    /**
     * Retrieve the priority for the message.
     *
     * @return the priority
     */
    public final LogCatPriority getPriority() {
        return priority;
    }

    /**
     * Retrieve the Process ID.
     *
     * @return the process ID
     */
    public final int getPid() {
        return pid;
    }

    /**
     * Retrieve the Thread ID.
     *
     * @return the thread id
     */
    public final int getTid() {
        return tid;
    }

    /**
     * Retrieve the Tag.
     *
     * @return the tag
     */
    public final String getTag() {
        return tag;
    }

    /**
     * Retrieve the message.
     *
     * @return the message
     */
    public final String getMessage() {
        return message;
    }

    /**
     * Retrieve the raw message.
     *
     * @return the raw message
     */
    final String getRaw() {
        return raw;
    }

    /**
     * Parses a message received from log cat into a {@link LogCatMessage} object.
     *
     * @param message the message as a string
     * @return the {@link LogCatMessage}
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    static LogCatMessage from(String message) {
        // Create the regex pattern
        final String regex = "(| +)([0-9]+)(\\.|)([0-9]{3}|)\\s+([0-9]+)\\s+([0-9]+)\\s" +
                             "([VDIWEF])\\s([^:]*):\\s+(.*)";
        final Pattern pattern = Pattern.compile(regex);

        // Determine if the message matches the pattern
        final Matcher matcher = pattern.matcher(message);
        if (! matcher.find()) {
            return null;
        }

        // Extract the data required from the matcher.
        String timestampString = matcher.group(2);
        String subSecondString = matcher.group(4);
        String pidString       = matcher.group(5);
        String tidString       = matcher.group(6);
        String priorityString  = matcher.group(7);
        String tagString       = matcher.group(8);
        String messageString   = matcher.group(9);

        // Parse the milliseconds value if one exists.
        long milliseconds = 0;
        if (subSecondString != null && subSecondString.trim().length() > 0) {
            milliseconds = (long) (Float.parseFloat("0." + subSecondString) * 1000F);
        }

        // Extract the priority from the message.
        int  priority = LogCatPriority.getNumericFromCharacter(priorityString);
        if (priority == -1) {
            return null;
        }

        // Extract the timestamp from he message.
        long timestamp = (Long.parseLong(timestampString) * 1000L) + milliseconds;

        // Extract the process ID and the thread ID from the message.
        int  pid       = Integer.parseInt(pidString);
        int  tid       = Integer.parseInt(tidString);

        // Compile the output LogCatMessage object.
        LogCatMessage output = new LogCatMessage();
        output.loggedAt = new Date(timestamp);
        output.pid = pid;
        output.tid = tid;
        output.priority = new LogCatPriority(priority);
        output.tag = tagString;
        output.message = messageString;
        output.raw = message;

        return output;
    }
}
