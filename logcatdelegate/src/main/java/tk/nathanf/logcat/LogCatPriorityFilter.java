package tk.nathanf.logcat;

import java.util.ArrayList;
import java.util.List;

public class LogCatPriorityFilter extends LogCatMessageFilter {
    public LogCatPriorityFilter(List<LogCatPriority> priorities) {
        super(prioritiesToValidRegex(priorities));
        super.setForcePriorityMessageSpan(true);
    }

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
