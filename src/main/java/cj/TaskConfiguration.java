package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping
@StaticInitSafe
public interface TaskConfiguration {
    String name();

    String description();

    String maturityLevel();

    List<InputConfig> inputs();
}
