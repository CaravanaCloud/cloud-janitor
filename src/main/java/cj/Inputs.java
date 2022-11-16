package cj;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@ApplicationScoped
public class Inputs {
    @Inject
    Configuration configuration;

    Map<Input, InputConfig> inputConfigs = new HashMap<>();

    public Inputs putConfig(Input input,
                            String configKey,
                            Function<Configuration, Optional<String>> configFn,
                            Supplier<String> defaultFn){
        var inputConfig = new InputConfig(input, configKey, configFn, defaultFn);
        inputConfigs.put(input, inputConfig);
        return this;
    }

    public String getFromConfig(Input input) {
        var inputConfig = inputConfigs.get(input);
        if (inputConfig != null)
            return inputConfig.applyConfigFn(configuration).orElse(null);
        return null;
    }

    public String getFromDefault(Input input) {
        var inputConfig = inputConfigs.get(input);
        if (inputConfig != null)
            return inputConfig.applyDefaultFn();
        return null;
    }

    public InputConfig getConfig(Input inputKey) {
        return inputConfigs.get(inputKey);
    }
}
