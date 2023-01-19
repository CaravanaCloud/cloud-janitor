package cj;

import cj.ocp.CapabilityNotFoundException;
import cj.qute.Templates;
import cj.reporting.Reporting;
import cj.spi.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cj.Errors.Type.Message;

@ApplicationScoped
@Named("tasks")
public class Tasks {
    static final LocalDateTime startTime = LocalDateTime.now();

    // @Inject
    Logger log = LoggerFactory.getLogger(Tasks.class);

    @Inject
    Configuration config;

    @Inject
    Reporting reporting;

    List<Task> history = new ArrayList<>();

    String task;

    @Inject
    InputsMap inputsMap;

    @Inject
    Objects objects;

    @Inject
    Templates templates;

    @Inject
    Shell shell;

    @Inject
    Repeat repeat;

    // run methods

    public void run(List<String> args){
        var arr = args.toArray(new String[0]);
        run(arr);
    }

    public void run(String[] args) {
        init();
        repeat(args);
        report();
    }

    private void init() {
        log.debug("{}", config);
    }

    private void repeat(String... args) {
        var query = queryFrom(args);
        var queryList = List.of(query);
        var repeater = repeat.forQuery(query);
        submitTask(repeater);
    }

    private String[] queryFrom(String[] args) {
        if (args != null && args.length > 0)
            return args;
        var cfgTask = config.raw().task();
        if (cfgTask.isPresent()) {
            var query = cfgTask.get().split(" ");
            return query;
        }
        return new String[]{};
    }

    public void submitQuery(List<String> query) {
        submitQuery(query, Map.of());
    }
    public void submitQuery(List<String> query, Map<Input, Object> inputs) {
        var arr = query.toArray(new String[0]);
        var tasks = config.lookupTasks(arr);
        inputs = new HashMap<>(inputs);
        inputs.put(CJInput.query, query);
        submitAll(tasks, inputs);
    }
    private void submitAll(List<? extends Task> tasks, Map<Input, Object> inputs) {
        tasks.forEach(t -> submitTask(t.withInputs(inputs)));
    }

    // TODO: Consider async execution
    public Task submitTask(Task task) {
        try {
            var result = submitNow(task);
            return result;
        } catch (Exception e) {
            log.info("Exception running task {}: {}", task, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    public Task submitNow(Task task) {
        runDependencies(task);
        //TODO: convert bypass to task so that it render templates
        renderTemplates(task);
        runSingle(task);
        return task;
    }

    private void renderTemplates(Task task) {
        var taskConfig = config.taskConfigForTask(task);
        var templatesCfgs = taskConfig.map(c -> c.templates())
                .orElse(List.of());
        templatesCfgs.forEach(t -> {
            templates.render(task, t.template(), t.output());
        });
    }

    private void runDependencies(Task task) {
        var thisInputs = task.inputs();
        var dependencies = task.getDependencies();
        dependencies.forEach(d -> {
            // TODO: Consider if dependencies should inherit inputs
            d.inputs().putAll(thisInputs);
            submitTask(d);
        });
    }

    // TODO: Consider retries
    // TODO: Consider thread synchronization
    public void runSingle(Task task) {
        if (task == null) {
            log.error("Task is null");
            return;
        }
        history.add(task);
        try {
            task.setStartTime(LocalDateTime.now());
            // TODO: Asynchronous Execution
            checkInputs(task);
            task.apply();
            log.trace("Executed {}", task);
            // TODO: General waiter
        } catch (CapabilityNotFoundException e) {
            var c = e.getCapability();
            log.warn("Capability not found: {}, try with -c '{}' or equivalent", c, c);
        } catch (ConfigurationNotFoundException e) {
            var msg = e.getMessage();
            log.error("ERROR");
            log.warn("WARN");
            log.info("INFO");
            log.debug("DEBUG");
            log.trace("TRACE");
            // System.out.println("System.out");
            // TODO: Why dont you log this?
            System.out.println(msg);
            log.error("Configuration not found: {}", msg);
            log.warn("Expected configuration not found.");
            log.warn(e.getMessage());
        } catch (TaskFailedException e) {
            log.warn("Task failed to complete: {}", e.getMessage());
            // TODO: How to signal failure across tasks? See RetryTask
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            task.getErrors().put(Message, e.getMessage());
            log.error("Error executing {}: {}", task, e.getMessage());
            throw new RuntimeException(e);
        } finally {
            task.setEndTime(LocalDateTime.now());
        }
    }

    // Reporting Support
    @SuppressWarnings("unused")
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    // Utilities
    @SuppressWarnings("unused")
    public String getStartTimeFmt() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startTime);
    }

    public synchronized String getExecutionId() {
        return config.getExecutionId();
    }

    private void report() {
        if (config.reportEnabled())
            try {
                reporting.report(this);
            } catch (Exception ex) {
                ex.printStackTrace();
                log.error("Reporting failed", ex);
            }
    }

    private void checkInputs(Task task) {
        List<Input> expected = inputsMap.getExpectedInputs(task);
        List<InputFunctions> missing = new ArrayList<>();
        Map<Input, String> present = new HashMap<>();
        for (var inputKey : expected) {
            var inputValue = task.input(inputKey)
                    .or(() -> inputsMap.fromConfig(inputKey));
            if (inputValue.isPresent()) {
                present.put(inputKey, inputValue.get().toString());
            } else {
                missing.add(inputsMap.getConfig(inputKey));
            }
        }
        if (!expected.isEmpty())
            log.debug("[{}] {} inputs expected, {} present, {} missing", task.getName(), expected.size(),
                    present.size(), missing.size());
        if (!missing.isEmpty()) {
            log.debug("[{}] inputs are missing: {}", missing.size(), missing);
            throw new ConfigurationNotFoundException(missing);
        }
    }


}