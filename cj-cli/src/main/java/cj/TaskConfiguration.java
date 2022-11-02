package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@ConfigMapping
@StaticInitSafe
public interface TaskConfiguration {
    String name();
}
