package tk.nathanf.logcat;

import android.util.Log;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class LogCatMessage {
    private Date loggedAt;
    private int priority;
    private int pid;
    private int tid;
    private String tag;
    private String message;

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
     * See {@link #getFormatted(String)} for information on format specifiers.
     * See {@link SimpleDateFormat} for information on DATE format specifiers.
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
        format = format.replace("%vi", Integer.toString(priority));
        format = format.replace("%vc", charForVerb(priority));
        format = format.replace("%v", nameForVerb(priority));
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
     * Retrieve the priority for the message. One of:
     * <ul>
     *     <li>{@link Log#ASSERT}</li>
     *     <li>{@link Log#ERROR}</li>
     *     <li>{@link Log#WARN}</li>
     *     <li>{@link Log#INFO}</li>
     *     <li>{@link Log#DEBUG}</li>
     *     <li>{@link Log#VERBOSE}</li>
     * </ul>
     *
     * @return the priority
     */
    public final int getPriority() {
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

    @SuppressWarnings("WeakerAccess")
    @Nullable
    static LogCatMessage parse(String message) {
        final String regex = "(| +)([0-9]+)(\\.|)([0-9]{3}|)\\s+([0-9]+)\\s+([0-9]+)\\s([V|D|I|W|E|A])\\s([^:]*):\\s+(.*)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(message);

        if (! matcher.find()) {
            return null;
        }

        String timestampString = matcher.group(2);
        String subSecondString = matcher.group(4);
        String pidString       = matcher.group(5);
        String tidString       = matcher.group(6);
        String verbString      = matcher.group(7);
        String tagString       = matcher.group(8);
        String messageString   = matcher.group(9);

        long milliseconds = 0;
        if (subSecondString != null && subSecondString.trim().length() > 0) {
            milliseconds = (long) (Float.parseFloat("0." + subSecondString) * 1000F);
        }

        int  verb      = -1;
        if (verbString != null) {
            verb = verbForString(verbString);
        }

        if (verb == -1) {
            return null;
        }

        long timestamp = (Long.parseLong(timestampString) * 1000L) + milliseconds;
        int  pid       = Integer.parseInt(pidString);
        int  tid       = Integer.parseInt(tidString);

        LogCatMessage output = new LogCatMessage();

        output.loggedAt = new Date(timestamp);
        output.pid = pid;
        output.tid = tid;
        output.priority = verb;
        output.tag = tagString;
        output.message = messageString;

        return output;
    }

    private static int verbForString(String in) {
        switch (in) {
            case "V":
                return Log.VERBOSE;
            case "D":
                return Log.DEBUG;
            case "I":
                return Log.INFO;
            case "W":
                return Log.WARN;
            case "E":
                return Log.ERROR;
            case "A":
                return Log.ASSERT;
            default:
                return -1;
        }
    }

    private static String charForVerb(int verb) {
        if (verb == Log.VERBOSE) {
            return "V";
        } else if (verb == Log.DEBUG) {
            return "D";
        } else if (verb == Log.INFO) {
            return "I";
        } else if (verb == Log.WARN) {
            return "W";
        } else if (verb == Log.ERROR) {
            return "E";
        } else if (verb == Log.ASSERT) {
            return "A";
        }
        return "";
    }

    private static String nameForVerb(int verb) {
        if (verb == Log.VERBOSE) {
            return "VERBOSE";
        } else if (verb == Log.DEBUG) {
            return "DEBUG";
        } else if (verb == Log.INFO) {
            return "INFO";
        } else if (verb == Log.WARN) {
            return "WARN";
        } else if (verb == Log.ERROR) {
            return "ERROR";
        } else if (verb == Log.ASSERT) {
            return "ASSERT";
        }
        return "";
    }
}
