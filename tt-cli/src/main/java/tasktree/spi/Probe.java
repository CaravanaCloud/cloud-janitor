package tasktree.spi;

import javax.inject.Named;
import java.util.Locale;
import java.util.concurrent.Callable;

public interface Probe<T> extends Callable<Sample<T>> {

    default String getName() {
        var named = getClass().getAnnotation(Named.class);
        var name = named == null ? getClass().getSimpleName() : named.value();
        return name;
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
