package tasktree.spi;

import javax.inject.Named;
import java.util.Locale;
import java.util.concurrent.Callable;

public interface Probe extends Callable<Sample> {

    default String getName() {
        var named = getClass().getAnnotation(Named.class);
        var name = named == null ? getClass().getSimpleName() : named.value();
        return name;
    }

    default boolean filter(String root){
        return getName().toLowerCase().startsWith(root.toLowerCase());
    }
}
