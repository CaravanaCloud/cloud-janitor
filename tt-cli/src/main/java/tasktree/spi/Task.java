package tasktree.spi;

import tasktree.Configuration;

import javax.inject.Named;
import java.util.List;
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
}
