package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.Map;
import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface TaskConfiguration {
    String name();
}
