package cj.hello;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface HelloConfiguration {
    Optional<String> message();
}
