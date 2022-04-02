package tasktree.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.Configuration;
import tasktree.Result;
import tasktree.visitor.Visitor;

import javax.inject.Named;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public interface Task extends Runnable{

    void runSafe();
    List<Task> getSubtasks();

    Configuration getConfig();
    void setConfig(Configuration config);

    @Override
    default void run() {
        getConfig().runTask(this);
    }

    default int getRetries(){return 0;};

    void retried();


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

    String defaultPrefix = "tasktree.";
    default boolean filter(String root){
        var className = getClass().getName().toLowerCase();
        var rootName = root.toLowerCase();
        var result = className.startsWith(rootName)
                || className.startsWith(defaultPrefix + rootName);
        return result;
    }

    default boolean isWrite(){
        return true;
    };

    default void accept(Visitor visitor) {
        if (! isWrite())
            visitor.read(this);
        for(Task child : getSubtasks()) {
            child.accept(visitor);
        }
        if (isWrite())
            visitor.write(this);
    }

    default Result getResult(){
        return Result.empty(this);
    }

    void setResult(Result result);

    default String getDescription(){
        return "";
    }

    default LocalDateTime getStartTime(){
        return LocalDateTime.now();
    }

    default LocalDateTime getEndTime(){
        return LocalDateTime.now();
    }

    default Duration getElapsedTime(){
        return Duration.between(getStartTime(), getEndTime());
    }

    default Optional<Long> getWaitAfterRun(){
        return Optional.empty();
    }

    default void info(String message, String... args){
        getLogger().info(message, new Object[]{args});
    }

    default void error(String message, String... args){
        getLogger().error(message, new Object[]{args});
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getName());
    }

    default void debug(String message, String... args){
        getLogger().debug(message, new Object[]{args});
    }

}
