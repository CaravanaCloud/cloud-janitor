package tasktree.spi;

import software.amazon.awssdk.services.ec2.model.Instance;
import tasktree.Configuration;

import javax.inject.Named;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface Task
        extends Runnable {

    void setConfig(Configuration config);

    default int getRetries(){return 0;};
    void retried();

    default String getSimpleName(){
        return getClass().getSimpleName().split("_")[0];
    }

    default String getPackage(){
        return getClass().getPackage().getName();
    }

    default String getName() {
        return getClass().getCanonicalName();
    }

    static final String defaultPrefix = "tasktree.";
    default boolean filter(String root){
        var className = getClass().getName().toLowerCase();
        var rootName = root.toLowerCase();
        var result = className.startsWith(rootName)
                || className.startsWith(defaultPrefix + rootName);
        return result;
    }



}
