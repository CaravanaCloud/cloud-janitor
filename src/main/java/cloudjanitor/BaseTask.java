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
    protected Tasks tasks;

    Optional<LocalDateTime> startTime = Optional.empty();
    Optional<LocalDateTime> endTime = Optional.empty();
    Map<String, Object> outputs = new HashMap<>();
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
    public Map<String, Object> getOutputs() {
        return outputs;
    }

    @Override
    public Map<String, Object> getErrors() {
        return errors;
    }

    /* Utility Methods */
    @Deprecated
    protected void success(String key, Object value){
        outputs.put(key, value);
    }

    protected void success(Output output, Object value){
        outputs.put(output.name(), value);
    }

    protected void failure(String message) {
        getErrors().put("message",message);
    }

    public Optional<Object> findOutput(String key) {
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

    public List findAsList(String key) {
        return findOutput(key)
                .map( o -> (List) o)
                .orElse(List.of());
    }

    @Override
    public Optional<String> findString(String key) {
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
}
