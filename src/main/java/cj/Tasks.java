package cj;

import cj.fs.FSUtils;
import cj.ocp.CapabilityNotFoundException;
import cj.qute.Templates;
import cj.reporting.Reporting;
import cj.shell.*;
import cj.spi.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static cj.Errors.Type.Message;
import static cj.StringUtils.join;
import static com.google.common.base.Preconditions.checkArgument;

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

    // run methods

    public void run(String[] args) {
        init();
        runTasks(args);
        report();
    }

    private void init() {
        log.trace("Configuration: {}", config);
    }

    private void runTasks(String[] args) {
        var tasks = config.lookupTasks(args);
        runAll(tasks);
    }

    private void runAll(List<? extends Task> tasks) {
        tasks.forEach(this::submit);
    }

    // TODO: Consider async execution
    public Task submit(Task task) {
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
        renderTemplates(task);
        runSingle(task);
        return task;
    }

    private void renderTemplates(Task task) {
        var template = objects.getAnnotation(task, TaskTemplate.class);
        if (template != null)
            renderTemplate(task, template.value(), template.output());
    }

    private void renderTemplate(Task task, String value, String output) {
        templates.render(task, value, output);
    }

    private void runDependencies(Task task) {
        var thisInputs = task.inputs();
        var dependencies = task.getDependencies();
        dependencies.forEach(d -> {
            // TODO: Consider if dependencies should inherit inputs
            d.inputs().putAll(thisInputs);
            submit(d);
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

    //////////////////////////////////////////////

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
                    .or(() -> fromConfig(inputKey));
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

    private Optional<?> fromConfig(Input input) {
        return Optional.ofNullable(inputsMap.getFromConfig(input));
    }

    public Path taskFile(Task task, String fileName) {
        return taskFile(task.getPathName(), fileName);
    }

    public Path taskFile(String taskName, String fileName) {
        return FSUtils.taskDir(taskName).resolve(fileName);
    }
}