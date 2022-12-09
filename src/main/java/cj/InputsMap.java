package cj;

import cj.spi.Task;
import io.quarkus.runtime.Startup;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@Startup
public class InputsMap {
    @Inject
    Configuration configuration;

    @Inject
    Logger log;

    Map<Input, InputConfig> inputConfigs = new HashMap<>();

    @Inject
    Objects beans;

    @PostConstruct
    @SuppressWarnings("unused")
    public void init(){
        log.trace("Initializing input mappings.");
    }
    public void putConfig(Input input,
                               String description,
                               String configKey,
                               Function<Configuration, Optional<?>> configFn,
                               Supplier<?> defaultFn,
                               String defaultDescription,
                               Object[] allowedValues,
                               boolean enrichBypass){
        var inputConfig = InputConfig.of(input,description, configKey, configFn, defaultFn, defaultDescription, allowedValues, enrichBypass);
        inputConfigs.put(input, inputConfig);
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
        if (inputConfig != null) {
            @SuppressWarnings("redundant")
            var result = inputConfig.applyDefaultFn();
            return result;
        }
        return null;
    }

    public InputConfig getConfig(Input inputKey) {
        return inputConfigs.get(inputKey);
    }

    public InputConfig findInputConfigByName(String inputName) {
        return inputConfigs.get(findInputByName(inputName));
    }

    public Input findInputByName(String inputName) {
        for (var inputConfig:inputConfigs.entrySet()){
            var key = inputConfig.getKey();
            if(key != null && key.toString().equals(inputName)){
                return key;
            }
        }
        log.warn("No input found for name {}.", inputName);
        return null;
    }

    public List<Input> getExpectedInputs(Task task) {
        return beans.getExpectedInputs(task);
    }

    public Optional<Object> valueOf(Task task, Input input) {
        checkNotNull(task);
        checkNotNull(input);
        var value = task.inputs().get(input);
        if (value == null) {
            value = valueOf(input);
        }
        return Optional.ofNullable(value);
    }

    public Object valueOf(Input input) {
        Object value = getFromConfig(input);
        if (value == null) {
            value = getFromDefault(input);
        }
        return value;
    }
}
