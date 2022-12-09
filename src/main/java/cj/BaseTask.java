package cj;

import cj.fs.FSInput;
import cj.fs.FindFiles;
import cj.ocp.CapabilityNotFoundException;
import cj.qute.Templates;
import cj.spi.Task;
import io.quarkus.qute.Template;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cj.Errors.Type;
import static cj.Errors.Type.Message;
import static cj.Output.local.FilesMatch;

@Dependent
public class BaseTask
        implements Task,
            TaskManagement,
            Logging {
    @Inject
    Configuration config;

    LocalDateTime startTime = null;
    LocalDateTime endTime = null;

    Map<Input, Object> inputs = new HashMap<>();
    Map<Output, Object> outputs = new HashMap<>();
    Map<Errors, Object> errors = new HashMap<>();

    @Inject
    InputsMap inputsMap;

    @Inject
    Templates templates;
    LocalDateTime createTime = LocalDateTime.now();


    /* Delegate Methods */

    /* TaskManagement Delegates */
    protected Task submit(Task delegate) {
        return submit(this, delegate);
    }

    /* Task Properties */
    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    @Override
    public void setStartTime(LocalDateTime localDateTime) {
        startTime = localDateTime;
    }

    /******* REFACTORING RULER **********/
    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /* Input, Output and Errors */

    @Override
    public void setEndTime(LocalDateTime localDateTime) {
        this.endTime = localDateTime;
    }

    public Optional<Object> output(Output key) {
        var result = Optional.ofNullable(getOutputs().get(key));
        if (result.isEmpty()) {
            for (Task dep : getDependencies()) {
                result = dep.output(key);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> outputList(Output key, Class<T> valueClass) {
        var output = output(key);
        if (output.isEmpty()) {
            return List.of();
        } else {
            var outputValue = output.get();
            if (outputValue instanceof List<?> outputList) {
                return (List<T>) outputList;
            } else {
                throw new RuntimeException("Output " + key + " is not a List<" + valueClass.getName() + ">");
            }
        }
    }




    @SuppressWarnings("all")
    public <T> List<T> inputList(Input key, Class<T> valueClass) {
        var in = input(key);
        if (in.isPresent()) {
            var val = in.get();
            if (val instanceof List<?> vals) {
                @SuppressWarnings("unchecked")
                var result = (List<T>) vals;
                return result;
            } else {
                throw new IllegalArgumentException("Input " + key + " is not a list");
            }
        } else
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

    @Override
    public String toString() {
        return "%s ".formatted(
                getSimpleName());

    }

    public Configuration config() {
        return config;
    }

    @Override
    public Map<Input, Object> inputs() {
        return inputs;
    }

    public String expectInputString(Input key) {
        return inputString(key).orElseThrow();
    }

    public String getInputString(Input key) {
        return getInputString(key, null);
    }

    public String getInputString(Input key, String defaultValue) {
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

    public Optional<Object> input(Input key) {
        return inputsMap.valueOf(this, key);
    }

    @SuppressWarnings("all")
    public <T> Optional<T> inputAs(Input key, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        var result = (Optional<T>) input(key);
        return result;
    }

    public <T> Optional<T> outputAs(Output key, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        var result = (Optional<T>) Optional.ofNullable(outputs.get(key));
        return result;
    }

    @SuppressWarnings("all")
    public <T> T getInput(Input key, Class<T> inputClass) {
        var result = input(key);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Failed to resolve expected input " + key);
        }
        @SuppressWarnings("unchecked")
        T t = (T) result.get();
        return t;
    }

    public String matchMark(boolean match) {
        return match ? "X" : "O";
    }

    public List<Task> delegateAll(Task... tasks) {
        return Stream.of(tasks)
                .map(t -> t.withInputs(inputs()))
                .toList();
    }

    public Task delegate(Task task) {
        return task.withInputs(inputs());
    }

    public void submitAll(Task... delegates) {
        Stream.of(delegates).forEach(this::submit);
    }

    public void submitAll(List<Task> delegates) {
        delegates.forEach(this::submit);
    }

    public Optional<String> inputString(Input key) {
        return input(key).map(Object::toString);
    }

    public Object output(Output key, Object value) {
        if (value instanceof Optional<?> opt) {
            value = opt.orElse(null);
        }
        if (value != null) {
            trace("{} / {} := {}", toString(), key.toString(), value.toString());
            return outputs.put(key, value);
        } else
            return null;
    }

    protected Task submitInstance(Instance<? extends Task> delegate, Input input, Object value) {
        return tasks().submit(delegate.get().withInput(input, value));
    }

    protected Task submit(Task delegate, Input input, Object value) {
        return tasks().submit(delegate.withInput(input, value));
    }

    protected void success(Output key, Object value) {
        output(key, value);
    }

    protected void success() {
        trace("Task success(): {}", this);
    }


    protected RuntimeException fail(String message, Object... args) {
        var msg = fmt(message).formatted(args);
        error(msg);
        getErrors().put(Message, msg);
        return new TaskFailedException(msg);
    }


    protected RuntimeException fail(Exception ex) {
        log().error(ex.getMessage(), ex);
        if (Configuration.PRINT_STACK_TRACE) {
            ex.printStackTrace();
        }
        getErrors().put(Type.Exception, ex);
        return new RuntimeException(ex);
    }


    protected String getExecutionId() {
        return tasks().getExecutionId();
    }

    protected Task withInput(Instance<? extends Task> instance, Input input, Object value) {
        if (instance == null || input == null || value == null) {
            throw new RuntimeException("Somebody is null");
        }
        try {
            var task = instance.get();
            task = task.withInput(input, value);
            return task;
        } catch (Exception ex) {
            ex.printStackTrace();
            debug("Failed to create task instance for {}", instance);
            throw new RuntimeException(ex);
        }
    }

    /*
    protected Path getTaskDir(String context) {
        return FSUtils.taskDir(this, context);
    }
    */

    protected boolean hasCapabilities(Capabilities... cs) {
        return tasks().hasCapabilities(cs);
    }

    protected <T> void forEach(List<T> list, Consumer<T> consumer) {
        var stream = list.stream();
        if (config().parallel()) {
            stream = stream.parallel();
        }
        stream.forEach(consumer);
    }

    protected Template getTemplate(String location) {
        return templates.getTemplate(location);
    }


    protected void expectCapability(@SuppressWarnings("SameParameterValue") Capabilities capability) {
        if (!hasCapabilities(capability)) {
            debug("Missing capability {} ", capability);
            throw new CapabilityNotFoundException(capability);
        }
    }


    protected void checkpoint(String message,
                              Object... args){

        info(message, args);
        var sleep = config.checkpointSleep();
        if (sleep > 0){
            debug("Waiting {}s", sleep);
            try {
                Thread.sleep(sleep * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    protected <T> void tryParallel(List<T> list, Consumer<T> consumer) {
        tryParallel(list).forEach(consumer);
    }

    protected <T> Stream<T> tryParallel(List<T> list) {
        if (list == null) return Stream.empty();
        var result = list.stream();
        if (config().parallel()){
            result = result.parallel();
        }
        return result;
    }

    @Inject
    Instance<FindFiles> filterFiles;
    protected List<Path> findFiles(String extension) {
        return submitInstance(filterFiles, FSInput.extension, extension)
                .outputList(FilesMatch, Path.class);
    }


    protected String composeName(String... tokens) {
        return compose(namingSeparator(), altSeparator(), tokens);
    }
    protected String composeNameAlt(String... tokens) {
        return compose(altSeparator(), namingSeparator(),  tokens);
    }

    private String namingSeparator() {
        return config().namingSeparator().orElse("-");
    }

    private String altSeparator() {
        return config().altSeparator().orElse("_");
    }

    private String compose(String separator, String altSeparator, String[] context) {
        var name = composeNameSep(separator, context);
        name = name.replaceAll(altSeparator, separator);
        return name;
    }


    protected String composeNameSep(String separator, String... context) {
        var prefix = config().namingPrefix().orElse("");
        var result = prefix
                + separator
                + String.join(separator, context);
        return result;
    }

    protected String join(String separator, String... context) {
        return String.join(separator, context);
    }

    protected <T> T success(T result) {
        success(TaskOutput.main, result);
        return result;
    }

    protected Path render(String profile, String template, String output) {
        return templates.render(this, profile, template, output);
    }
    protected Path render(String template, String output) {
        return templates.render(this, template, output);
    }

    protected Path taskFile(String filename){
        return tasks().taskFile(this,filename);
    }
}
