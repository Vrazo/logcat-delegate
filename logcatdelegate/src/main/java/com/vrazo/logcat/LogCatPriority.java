package com.vrazo.logcat;

import android.util.Log;

/**
 * Class used to manage and represent log priorities.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LogCatPriority {
    private int priority;

    /**
     * Create a new LogCatPriority from it's numeric value.
     *
     * Valid values include:
     * <ul>
     *     <li>{@link Log#ASSERT}</li>
     *     <li>{@link Log#ERROR}</li>
     *     <li>{@link Log#WARN}</li>
     *     <li>{@link Log#INFO}</li>
     *     <li>{@link Log#DEBUG}</li>
     *     <li>{@link Log#VERBOSE}</li>
     * </ul>
     *
     * @param priority the priority
     */
    public LogCatPriority(int priority) {
        this.priority = priority;
        if (this.getCharacter().equals("")) {
            throw new RuntimeException(
                "Invalid priority code. Valid values include Log.ASSERT, Log.ERROR, Log.WARN, " +
                "Log.INFO, Log.DEBUG and Log.VERBOSE"
            );
        }
    }

    /**
     * Create a new LogCatPriority from it's character value.
     *
     * Valid values include V, D, I, W, E, and F.
     *
     * @param character the chracter
     */
    public LogCatPriority(String character) {
        this.priority = getNumericFromCharacter(character);
        if (this.priority == -1) {
            throw new RuntimeException(
                "Invalid character value for priority. Valid values include V, D, I, W, E, and F"
            );
        }
    }

    /**
     * Retrieve the character version of this priority.
     *
     * @return the character
     */
    public final String getCharacter() {
        return getCharacterFromNumeric(this.priority);
    }

    /**
     * Retrieve the name for this priority. Name will be in all upper-case.
     *
     * @return the name
     */
    public final String getName() {
        return getNameFromNumeric(this.priority);
    }

    /**
     * Retrieve the numeric representation of this priority.
     *
     * @return the numeric representation
     */
    public final int getNumeric() {
        return this.priority;
    }

    /**
     * Retrieve the numeric value for a priority specified in the input.
     *
     * Input should be one of "V, D, I, W, E, or F".
     *
     * Output will be One of:
     * <ul>
     *     <li>{@link Log#ASSERT}</li>
     *     <li>{@link Log#ERROR}</li>
     *     <li>{@link Log#WARN}</li>
     *     <li>{@link Log#INFO}</li>
     *     <li>{@link Log#DEBUG}</li>
     *     <li>{@link Log#VERBOSE}</li>
     * </ul>
     *
     * @param in the input value.
     * @return the numeric value
     */
    public static int getNumericFromCharacter(String in) {
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
            case "F":
                return Log.ASSERT;
            default:
                return -1;
        }
    }

    /**
     * Retrieve the single character representation for the specified priority.
     *
     * Input should be one of:
     * <ul>
     *     <li>{@link Log#ASSERT}</li>
     *     <li>{@link Log#ERROR}</li>
     *     <li>{@link Log#WARN}</li>
     *     <li>{@link Log#INFO}</li>
     *     <li>{@link Log#DEBUG}</li>
     *     <li>{@link Log#VERBOSE}</li>
     * </ul>
     *
     * Output will be the corresponding character, one of "V, D, I, W, E, or F".
     *
     * @param priority the input priority
     *
     * @return the output character
     */
    public static String getCharacterFromNumeric(int priority) {
        if (priority == Log.VERBOSE) {
            return "V";
        } else if (priority == Log.DEBUG) {
            return "D";
        } else if (priority == Log.INFO) {
            return "I";
        } else if (priority == Log.WARN) {
            return "W";
        } else if (priority == Log.ERROR) {
            return "E";
        } else if (priority == Log.ASSERT) {
            return "F";
        }
        return "";
    }

    /**
     * Retrieve the name for the specified priority.
     *
     * Input should be one of:
     * <ul>
     *     <li>{@link Log#ASSERT}</li>
     *     <li>{@link Log#ERROR}</li>
     *     <li>{@link Log#WARN}</li>
     *     <li>{@link Log#INFO}</li>
     *     <li>{@link Log#DEBUG}</li>
     *     <li>{@link Log#VERBOSE}</li>
     * </ul>
     *
     * @param priority the input priority
     * @return the name
     */
    public static String getNameFromNumeric(int priority) {
        if (priority == Log.VERBOSE) {
            return "VERBOSE";
        } else if (priority == Log.DEBUG) {
            return "DEBUG";
        } else if (priority == Log.INFO) {
            return "INFO";
        } else if (priority == Log.WARN) {
            return "WARN";
        } else if (priority == Log.ERROR) {
            return "ERROR";
        } else if (priority == Log.ASSERT) {
            return "ASSERT";
        }
        return "";
    }
}
