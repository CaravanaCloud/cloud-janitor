package cj.spi;

import cj.Errors;
import cj.Input;
import cj.Logs;
import cj.Output;

import javax.inject.Named;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A task is a function to be executed, as a basic unit of work.
 */
public interface Task {
    /**
     * The code to be executed by the Task, if considered *safe* by the TaskManager.
     */
    default void apply() {
        throw new RuntimeException("Task not implemented");
    }

    /**
     * Task start time, if started
     */
    default LocalDateTime getStartTime() {
        return null;
    }

    default void setStartTime(LocalDateTime localDateTime) {
        throw new RuntimeException("Method not implemented");
    }

    /**
     * Task end time, if finished
     */
    default LocalDateTime getEndTime() {
        return null;
    }

    default void setEndTime(LocalDateTime localDateTime) {
        throw new RuntimeException("Method not implemented");
    }

    /**
     * Elapsed Time between Start and End, if finished
     */
    default Optional<Duration> getElapsedTime() {
        var start = getStartTime();
        var end = getEndTime();
        if (start != null && end != null) {
            return Optional.of(Duration.between(start, end));
        } else return Optional.empty();
    }

    /* Task Chaining */
    default List<Task> getDependencies() {
        var dep = getDependency();
        if (dep != null)
            return List.of(dep);
        else
            return List.of();
    }

    default Task getDependency() {
        return null;
    }

    default Map<Input, Object> getInputs() {
        return Map.of();
    }

    default Task withInputs(Map<Input, Object> inputs) {
        return this;
    }

    default Task withInput(Input key, Object value) {
        return this;
    }


    default Map<Output, Object> getOutputs() {
        return Map.of();
    }

    default Optional<Object> output(Output key) {
        return Optional.empty();
    }

    default Optional<String> outputString(Output key) {
        return Optional.empty();
    }

    default Map<Errors, Object> getErrors() {
        return Map.of();
    }


    /* Task Naming */
    default String getSimpleName() {
        return getClass().getSimpleName().split("_")[0];
    }

    default String getPackage() {
        return getClass().getPackage().getName();
    }

    default String getName() {
        var name = getSimpleName();
        var named = getClass().getAnnotation(Named.class);
        if (named != null) {
            name = named.value();
        }
        var superclass = getClass().getSuperclass();
        named = superclass.getAnnotation(Named.class);
        if (named != null) {
            name = named.value();
        }
        return name;
    }

    /* Logging */
    default String getLoggerName() {
        var name = getClass().getName();
        return Logs.loggerName(name);
    }

    @SuppressWarnings("unused")
    default String getStartTimeFmt() {
        return getDateFormat().format(getStartTime());
    }

    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    default DateTimeFormatter getDateFormat() {
        return dateTimeFormat;
    }

    @SuppressWarnings("unused")
    default String getDurationFmt() {
        return getElapsedTime()
                .map(duration -> {
                    var secs = duration.getSeconds();
                    if (secs > 0) return String.format("%ds", secs);
                    else return String.format("0.%03ds", duration.toMillis());
                })
                .orElse("?");
    }

    @SuppressWarnings("unused")
    default String getClassName() {
        return getClass().getName().split("_")[0];
    }

    default boolean isSuccess() {
        return getErrors().isEmpty();
    }

    default Optional<Object> input(Input key) {
        return Optional.empty();
    }

    default <T> Optional<T> outputAs(Output key, Class<T> clazz) {
        return Optional.empty();
    }

    default <T> List<T> outputList(Output key, Class<T> valueClass) {
        return List.of();
    }
}
