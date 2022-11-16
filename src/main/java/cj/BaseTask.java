package cj;

import cj.fs.FSUtils;
import cj.shell.ShellTask;
import cj.spi.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static cj.CJInput.*;
import static cj.Errors.Type;
import static cj.Errors.Type.Message;
import static cj.shell.ShellInput.*;
@Dependent
public class BaseTask implements Task {
    @Inject
    transient Tasks tasks;

    @Inject
    Configuration config;

    @Inject
    Instance<RetryTask> retry;

    @Inject
    Instance<ShellTask> shellInstance;


    //TODO: Use null instead of Optional in fields
    Optional<LocalDateTime> startTime = Optional.empty();
    Optional<LocalDateTime> endTime = Optional.empty();

    Map<Input, Object> inputs = new HashMap<>();
    Map<Output, Object> outputs = new HashMap<>();
    Map<Errors, Object> errors = new HashMap<>();

    /* Submits a delegate task for execution */
    public Task submit(Task delegate){
        delegate.getInputs().putAll(getInputs());
        return tasks.submit(delegate);
    }

    protected Task submitInstance(Instance<? extends Task> delegate, Input input, Object value){
        return tasks.submit(delegate.get().withInput(input, value));
    }
    protected Task submit(Task delegate, Input input, Object value){
        return tasks.submit(delegate.withInput(input, value));
    }


    /* Task Interface Methods */
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

    /* Input, Output and Errors */

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
        var output = output(key);
        if (output.isEmpty()){
            return List.of();
        }else{
            var outputValue = output.get();
            if (outputValue instanceof List outputList){
                return (List<T>) outputList;
            }else {
                throw new RuntimeException( "Output " + key + " is not a List<"+valueClass.getName()+">");
            }
        }
    }
    @SuppressWarnings("all")
    public <T> List<T> inputList(Input key, Class<T> valueClass) {
        var in = input(key);
        if (in.isPresent()){
            var val = in.get();
            if (val instanceof List<?>){
                return (List<T>) val;
            } else {
                throw new IllegalArgumentException("Input " + key + " is not a list");
            }
        }else
            return List.of();
    }


    @Override
    public Optional<String> outputString(Output key) {
        return output(key)
                .map(Object::toString);
    }

    @Override
    public Map<Output, Object> getOutputs() {
        return outputs;
    }

    @Override
    public Map<Errors, Object> getErrors() {
        return errors;
    }

    protected void success(Output key, Object value){
        output(key, value);
    }

    protected void success(){
        trace("Task success(): {}", this);
    }

    /* Logging Shortcuts */
    protected void info(String message, Object... args){
        logger().info(fmt(message), args);
    }
    protected void trace(String message, Object... args){
        logger().trace(fmt(message), args);
    }
    protected void debug(String message, Object... args){
        logger().debug(fmt(message), args);
    }

    protected void error(String message, Object... args){
        logger().error(fmt(message), args);
    }

    protected RuntimeException fail(String message, Object... args) {
        var msg = fmt(message).formatted(args);
        error(msg);
        getErrors().put(Message ,msg);
        return new TaskFailedException(msg);
    }

    private String fmt(String message) {
        var context = getContextString();
        var separator = context.isEmpty() ? "" : getContextSeparator();
        return context + separator + message;
    }

    protected String getContextString() {
        return "";
    }

    private String getContextSeparator() {
        return " || ";
    }

    protected RuntimeException fail(Exception ex) {
        logger().error(ex.getMessage(), ex);
        if( Configuration.PRINT_STACK_TRACE){
            ex.printStackTrace();
        }
        getErrors().put(Type.Exception , ex);
        return new RuntimeException(ex);
    }

    protected void warn(String message, Object... args) {
        logger().warn(fmt(message), args);
    }
    protected void warn(Exception ex, String message, Object... args) {
        warn(ex.getMessage());
        warn(fmt(message), args);
    }


    protected Logger logger() {
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


    @Override
    public Map<Input, Object> getInputs(){
        return inputs;
    }

    public String expectInputString(Input key){
        return inputString(key).orElseThrow();
    }

    public String getInputString(Input key){
        return getInputString(key, null);
    }

    public String getInputString(Input key, String defaultValue){
        return inputAs(key, String.class).orElse(defaultValue);
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
        if (key == null) return Optional.empty();
        var value = inputs.get(key);
        if (value == null) {
            value = cfgInputString(key);
        }
        if (value == null) {
            value = inputss.getFromDefault(key);
        }
        if (value == null) {
            value = tasks.getCLIInput(key.toString());
        }
        return Optional.ofNullable(value);
    }

    //TODO move to task inputs
    @Inject
    Inputs inputss;
    public String cfgInputString(Input key){
        return inputss.getFromConfig(key);
    }

    @SuppressWarnings("all")
    public <T> Optional<T> inputAs(Input key, Class<T> clazz){
        return (Optional<T>) input(key);
    }

    @SuppressWarnings("all")
    public <T> Optional<T> outputAs(Output key, Class<T> clazz){
        return (Optional<T>) Optional.ofNullable(outputs.get(key));
    }

    @SuppressWarnings("all")
    public <T> T getInput(Input key, Class<T> inputClass){
        return (T) input(key).orElseThrow();
    }

    public String matchMark(boolean match){
        return match ? "X" : "O";
    }

    public List<Task> delegateAll(Task... tasks) {
        return Stream.of(tasks)
                .map(t -> t.withInputs(getInputs()))
                .toList();
    }

    public Task delegate(Task task){
        return task.withInputs(getInputs());
    }

    public void submitAll(Task... delegates){
        Stream.of(delegates).forEach(this::submit);
    }
    public void submitAll(List<Task> delegates){
        delegates.forEach(this::submit);
    }

    public Optional<String> inputString(Input key){
        return input(key).map(Object::toString);
    }

    public Object output(Output key, Object value){
        if (value instanceof Optional<?> opt){
            value = opt.orElse(null);
        }
        if (value != null) {
            trace("{} / {} := {}", toString(), key.toString(), value.toString());
            return outputs.put(key, value);
        } else return null;
    }


    protected String getExecutionId(){
        return tasks.getExecutionId();
    }

    protected void retry(Task theMainTask, Task theFixTask) {
        var ccoctlTask = retry.get()
                .withInput(taskName, theMainTask)
                .withInput(fixTask, theFixTask);
        submit(ccoctlTask);
    }

    protected Task withInput(Instance<? extends Task> task, Input input, Object value) {
        return task.get().withInput(input, value);
    }

    protected Path getTaskDir(String dirName) {
        return FSUtils.getTaskDir(getContextName(), dirName);
    }

    private String getContextName() {
        return getClass().getPackageName().replaceAll("cj.","");
    }

    protected Optional<String> exec(String... cmdArgs){
        return exec(DEFAULT_TIMEOUT_MINS, false, cmdArgs);
    }

    @SuppressWarnings("unused")
    protected Optional<String> exec(Boolean dryRun, String... cmdArgs){
        return exec(DEFAULT_TIMEOUT_MINS, dryRun, cmdArgs);
    }

    @SuppressWarnings("all")
    protected Optional<String> exec(Long timeout, String... cmdArgs){
        return exec(timeout, false, cmdArgs);
    }

    protected Optional<String> exec(Long timeoutMins, Boolean isDryRun, String... cmdArgs){
        if (cmdArgs.length == 1){
            var cmd = cmdArgs[0];
            if (cmd.contains(" ")){
                cmdArgs = cmd.split(" ");
            }
        }
        var shellTask = shellTask(isDryRun, cmdArgs)
                .withInput(timeout, timeoutMins);
        submit(shellTask);
        @SuppressWarnings("all")
        var output = shellTask.outputString(Output.shell.stdout);
        return output;
    }

    protected ShellTask shellTask(String... cmdArgs) {
        return shellTask(false , cmdArgs);
    }

    protected ShellTask shellTask(Boolean isDryRun, String... cmdArgs) {
        var cmdList = Arrays.asList(cmdArgs);
        var shellTask = shellInstance.get();
        if (isDryRun != null){
            shellTask.withInput(dryRun, isDryRun);
        }
        shellTask.withInput(cmds, cmdList);
        return shellTask;
    }
}
