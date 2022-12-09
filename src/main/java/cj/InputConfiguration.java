package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface InputConfiguration {
    String name();

    Optional<String> description();

    Optional<String> configKey();
    default Optional<String> getEnvVarName(){
        var result = configKey()
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", "_")
                        .toUpperCase());
        return result;
    }
    Optional<String> defaultDescription();

    Optional<List<String>> allowedValues();
}
