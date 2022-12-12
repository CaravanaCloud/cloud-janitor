package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface TaskConfiguration {

    String name();

    Optional<String> description();

    Optional<String> maturity();

    Optional<List<InputConfiguration>> inputs();

    Optional<List<String>> bypass();
}
