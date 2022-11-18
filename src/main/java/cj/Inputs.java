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
                            Function<Configuration, Optional<?>> configFn,
                            Supplier<?> defaultFn){
        var inputConfig = new InputConfig(input, configKey, configFn, defaultFn);
        inputConfigs.put(input, inputConfig);
        return this;
    }

    public Object getFromConfig(Input input) {
        var inputConfig = inputConfigs.get(input);
        if (inputConfig != null) {
            @SuppressWarnings("all")
            var inputValue = inputConfig.applyConfigFn(configuration).orElse(null);
            return inputValue;
        }
        return null;
    }

    public Object getFromDefault(Input input) {
        var inputConfig = inputConfigs.get(input);
        if (inputConfig != null)
            return inputConfig.applyDefaultFn();
        return null;
    }

    public InputConfig getConfig(Input inputKey) {
        return inputConfigs.get(inputKey);
    }
}
