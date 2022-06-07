package cloudjanitor;

import cloudjanitor.spi.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;

@Dependent
public abstract class BaseTask implements Task {
    @Inject
    Tasks tasks;

    @Inject
    Configuration config;

    Optional<LocalDateTime> startTime = Optional.empty();
    Optional<LocalDateTime> endTime = Optional.empty();

    //TODO: Map<String, String> inputs = new HashMap<>();
    Map<Output, Object> outputs = new HashMap<>();
    Map<String, Object> errors = new HashMap<>();


    /* Interface Methods */
    @Override
    public Optional<LocalDateTime> getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(LocalDateTime localDateTime) {
        startTime = Optional.ofNullable(localDateTime);
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(LocalDateTime localDateTime) {
        this.endTime = Optional.ofNullable(localDateTime);
    }

    /* Task Chaining */

    @Override
    public Map<Output, Object> getOutputs() {
        return outputs;
    }

    @Override
    public Map<String, Object> getErrors() {
        return errors;
    }

    /* Utility Methods */
    protected void success(Output output, Object value){
        outputs.put(output, value);
    }

    protected void failure(String message) {
        getErrors().put("message",message);
    }

    public Optional<Object> findOutput(Output key) {
        var result = Optional.ofNullable(getOutputs().get(key));
        if (result.isEmpty()){
            for (Task dep: getDependencies()){
                result = dep.findOutput(key);
                if (result.isPresent()){
                    return result;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAsList(Output key, Class<T> valueClass) {
        return findOutput(key)
                .map(o -> (List<T>) o) 
                .orElse(List.of());
    }

    @Override
    public Optional<String> findString(Output key) {
        return findOutput(key)
                .map(o -> o.toString());
    }


    protected Logger log() {
        return LoggerFactory.getLogger(getLoggerName());
    }

    @Override
    public String toString() {
        return  "%s ".formatted(
                getSimpleName());

    }

    public Configuration getConfig() {
        return config;
    }

    public void runTask(Task task){
        tasks.runTask(task);
    }
}
