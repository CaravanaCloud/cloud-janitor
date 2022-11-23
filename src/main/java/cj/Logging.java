package cj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Logging {
    default Logger logger(){return LoggerFactory.getLogger(getClass());}

    default void info(String message, Object... args) {
        logger().info(fmt(message), args);
    }

    default void trace(String message, Object... args) {
        logger().trace(fmt(message), args);
    }

    default void debug(String message, Object... args) {
        logger().debug(fmt(message), args);
    }

    default void error(String message, Object... args) {
        logger().error(fmt(message), args);
    }

    default void warn(String message, Object... args) {
        logger().warn(fmt(message), args);
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

    default String getContextName() {
        return getClass().getPackageName().replaceAll("cj.", "");
    }
    default String getContextString() {
        return "";
    }

    default String getLoggerName() {
        var name = getClass().getName();
        return Logs.loggerName(name);
    }

}
