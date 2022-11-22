package cj;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface TaskMaturity {
    enum Level {
        experimental,
        stable
    }
    Level value() default Level.experimental;
}
