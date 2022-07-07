package cloudjanitor;

import cloudjanitor.spi.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Dependent
public abstract class BaseTask implements Task {
    @Inject
    Tasks tasks;

    @Inject
    Configuration config;

    Optional<LocalDateTime> startTime = Optional.empty();
    Optional<LocalDateTime> endTime = Optional.empty();

    Map<Input, Object> inputs = new HashMap<>();
    Map<Output, Object> outputs = new HashMap<>();
    Map<Errors, Object> errors = new HashMap<>();


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
    public Map<Errors, Object> getErrors() {
        return errors;
    }

    /* Utility Methods */
    protected void success(Output output, Object value){
        outputs.put(output, value);
        log().debug("{} / {} := {}", toString(), output.toString(), value.toString());
    }

    protected void success(){
        log().debug("Task success(): {}", toString());
    }

    protected void error(String message) {
        getErrors().put(Errors.Message ,message);
    }

    public Optional<Object> output(Output key) {
        var result = Optional.ofNullable(getOutputs().get(key));
        if (result.isEmpty()){
            for (Task dep: getDependencies()){
                result = dep.output(key);
                if (result.isPresent()){
                    return result;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> outputList(Output key, Class<T> valueClass) {
        return output(key)
                .map(o -> (List<T>) o) 
                .orElse(List.of());
    }

    @Override
    public Optional<String> outputString(Output key) {
        return output(key)
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

    public void submit(Task task){
        tasks.submit(task);
    }

    @Override
    public Map<Input, Object> getInputs(){
        return inputs;
    }

    public String getInputString(Input key){
        return getInputString(key, null);
    }

    public String getInputString(Input key, String defaultValue){
        var val = getInputs().get(key);
        if (val != null) return val.toString();
        else return defaultValue;
    }

    public Task withInput(Input key, Object value) {
        inputs.put(key, value);
        return this;
    }

    public Task withInputs(Map<Input, Object> inputs) {
        this.inputs = inputs;
        return this;
    }

    public Object withInput(Input key) {
        return inputs.get(key);
    }

    public Optional<Object> input(Input key){
        return Optional.ofNullable(inputs.get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> input(Input key, Class<T> clazz){
        return (Optional<T>) Optional.ofNullable(inputs.get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T getInput(Input key, Class<T> inputClass){
        return (T) input(key).get();
    }

    public String matchMark(boolean match){
        return match ? "X" : "O";
    }

    public List<Task> delegate(Task... tasks) {
        return Stream.of(tasks)
                .map(t -> t.withInputs(getInputs()))
                .toList();
    }

    public Optional<String> inputString(Input key){
        return input(key).map(o -> o.toString());
    }

    public Object output(Output key, Object value){
        return outputs.put(key, value);
    }

}
