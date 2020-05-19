package com.vrazo.logcat;

import java.util.List;

/**
 * A filter that will work based on message priority.
 */
public class LogCatPriorityFilter extends LogCatMessageFilter {
    /**
     * Create the priority filter with the specified priorities.
     *
     * @param priorities the priorities
     */
    public LogCatPriorityFilter(List<LogCatPriority> priorities) {
        super(prioritiesToValidRegex(priorities));
        super.setForcePriorityMessageSpan(true);
    }

    /**
     * Message span is not supported for {@link LogCatPriorityFilter}.
     *
     * @param messageSpan the message span
     */
    @Override
    public void setMessageSpan(MessageSpan messageSpan) {
        throw new RuntimeException("Message Span is not supported on LogCatPriorityFilter");
    }

    private static String prioritiesToValidRegex(List<LogCatPriority> priorities) {
        StringBuilder regex = new StringBuilder();

        for (LogCatPriority priority : priorities) {
            if (regex.length() > 0) {
                regex.append("|");
            }
            regex.append(priority.getCharacter());
        }

        return regex.toString();
    }
}
