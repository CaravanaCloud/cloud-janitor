package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface StepConfig {
    Optional<String> name();
    Optional<String> run();
}
