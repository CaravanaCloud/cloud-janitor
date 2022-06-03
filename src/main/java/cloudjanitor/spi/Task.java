package cloudjanitor.spi;

import cloudjanitor.Logs;

import javax.inject.Named;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A task is a function to be executed, as a basic unit of work.
 */
public interface Task {

    /**
     * The code to be executed by the Task, if considered *safe* by the TaskManager.
     */
    default void runSafe(){
        throw new RuntimeException("Task not implemented");
    }

    /**
     * A *write* task is only *safe* to execute if the *dry run* flag is false.
     */
    default boolean isWrite(){
        return true;
    };

    /**
     * Task start time, if started
     */
    default Optional<LocalDateTime> getStartTime(){
        return Optional.empty();
    }

    default void setStartTime(LocalDateTime localDateTime){
        throw new RuntimeException("Method not implemented");
    }

    /**
    * Task end time, if finished
     */
    default Optional<LocalDateTime> getEndTime(){
        return Optional.empty();
    }

    default void setEndTime(LocalDateTime localDateTime){
        throw new RuntimeException("Method not implemented");
    }

    /**
     * Elapsed Time between Start and End, if finished
     */
    default Optional<Duration> getElapsedTime(){
        var start = getStartTime();
        var end = getEndTime();
        if (start.isPresent() && end.isPresent()){
            return Optional.of(Duration.between(start.get(),end.get()));
        }else return Optional.empty();
    }

    /* Task Chaining */
    default List<Task> getDependencies(){
        return List.of();
    }

    default Map<String, Object> getOutputs(){
        return Map.of();
    }

    default Optional<Object> findOutput(String key){
        return Optional.empty();
    }
    default Optional<String> findString(String key) { return Optional.empty(); }

    default Map<String, Object> getErrors(){
        return Map.of();
    }


    /* Task Naming */
    default String getSimpleName(){
        return getClass().getSimpleName().split("_")[0];
    }

    default String getPackage(){
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

    /* Task Throttling */
    default Optional<Long> getWaitAfterRun() {
        return Optional.of(1_000L);
    }

    /* Logging */
    default String getLoggerName() {
        var name = getClass().getName();
        return Logs.loggerName(name);
    }

    default String getStartTimeFmt(){
        return getStartTime()
                .map(startTime -> getDateFormat().format(startTime))
                .orElse("?");
    }

    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    default DateTimeFormatter getDateFormat(){
        return dateTimeFormat;
    }

    default String getDurationFmt(){
        return getElapsedTime()
                .map(duration -> {
                    var secs = duration.getSeconds();
                    if (secs > 0) return String.format("%ds", secs);
                    else return String.format("0.%03ds", duration.toMillis());
                })
                .orElse("?");
    }

    default String getClassName(){
        return getClass().getName().split("_")[0];
    }

    default boolean isSuccess(){
        return getErrors().isEmpty();
    }
}
