package cj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Logging {
    default Logger log(){return LoggerFactory.getLogger(getClass());}

    default void info(String message, Object... args) {
        log().info(fmt(message), args);
    }

    default void trace(String message, Object... args) {
        log().trace(fmt(message), args);
    }

    default void debug(String message, Object... args) {
        log().debug(fmt(message), args);
    }

    default void error(String message, Object... args) {
        log().error(fmt(message), args);
    }

    default void warn(String message, Object... args) {
        log().warn(fmt(message), args);
    }

    default void warn(Exception ex, String message, Object... args) {
        warn(ex.getMessage());
        warn(fmt(message), args);
    }

    default String fmt(String message) {
        var context = getContextString();
        var separator = context.isEmpty() ? "" : getContextSeparator();
        return context + separator + message;
    }

    default String getContextSeparator() {
        return " || ";
    }


    default String getContextString() {
        return "";
    }

    /*
    default String getContextName() {
        return getClass().getPackageName().replaceAll("cj.", "");
    }

    default String getLoggerName() {
        var name = getClass().getName();
        return Logs.loggerName(name);
    }

     */

}
