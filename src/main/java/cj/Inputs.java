package cj;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ApplicationScoped
public class Inputs {
    @Inject
    Configuration configuration;

    Map<Input, Function<Configuration, Optional<String>>> fromConfigMap = new HashMap<>();
    public Inputs fromConfigFn(Input input, Function<Configuration, Optional<String>> fromConfigFn) {
        fromConfigMap.put(input, fromConfigFn);
        return this;
    }

    public Optional<String> fromConfig(Input input) {
        return fromConfigMap.get(input).apply(configuration);
    }
}
