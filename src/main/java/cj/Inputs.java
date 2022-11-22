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

@Singleton
@Startup
public class Inputs {
    @Inject
    Configuration configuration;

    @Inject
    Logger log;

    Map<Input, InputConfig> inputConfigs = new HashMap<>();

    @Inject
    Beans beans;

    @PostConstruct
    @SuppressWarnings("unused")
    public void init(){
        log.info("Initializing input mappings.");
    }
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
}
