package cj;

import cj.ocp.CapabilityNotFoundException;
import cj.reporting.Reporting;
import cj.spi.Task;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static cj.Errors.Type.Message;

@ApplicationScoped
public class Tasks {
    static final LocalDateTime startTime = LocalDateTime.now();

    //@Inject
    Logger log = LoggerFactory.getLogger(Tasks.class);

    @Inject
    BeanManager bm;

    @Inject
    Configuration config;

    @Inject
    Reporting reporting;

    Set<Capabilities> capabilities = new HashSet<>();

    List<Task> history = new ArrayList<>();

    String task;

    public void run() {
        log.trace("Tasks.run()");
        log.debug("Capabilities: {}", getCapabilities());
        log.debug("Parallel: {}", config.parallel());
        var tasks = loadTasks();
        for (var task : tasks) {
            run(task);
        }
        report();
    }

    private List<TaskConfiguration> loadTasks() {
        var tasks = new ArrayList<>(config.tasks());
        config.task().ifPresent(t -> addTaskByName(tasks, t));
        if (task != null) {
            addTaskByName(tasks, task);
        }
        return tasks;
    }

    private void addTaskByName(List<TaskConfiguration> tasks, String taskName) {
        if (taskName != null && !taskName.isEmpty()) {
            tasks.add(new SimpleTaskConfiguration(taskName));
        }
    }

    private void run(TaskConfiguration task) {
        log.info("Running task: {}", task.name());
        var matches = lookupTasks(task.name());
        runAll(matches);
    }

    private void report() {
        if (config.report().enabled())
            try {
                reporting.report(this);
            } catch (Exception ex) {
                ex.printStackTrace();
                log.error("Reporting failed", ex);
            }
    }

    private void runAll(List<Task> matches) {
        matches.forEach(this::submit);
    }

    private List<Task> lookupTasks(String taskName) {
        var tasks = bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .toList();
        log.debug("Loaded {} tasks with name {} ", tasks.size(), taskName);
        return tasks;
    }

    //TODO: Consider async execution
    public Task submit(Task task){
        @SuppressWarnings("redundant")
        var cf = submitFuture(task);
        try {
            @SuppressWarnings("redundant")
            var result = cf.get();
            return result;
        } catch (InterruptedException e) {
            log.error("Task interrupted", e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            log.error("Task execution exception", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Task exception", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public CompletableFuture<Task> submitFuture(Task task) {
        var thisInputs = task.getInputs();
        var dependencies = task.getDependencies();
        dependencies.forEach(d -> {
            //TODO: Consider if dependencies should inherit inputs
            d.getInputs().putAll(thisInputs);
            submit(d);
        });

        log.trace("Submitting future: {}", task);
        @SuppressWarnings("redundant")
        CompletableFuture<Task> cf = CompletableFuture.supplyAsync(() -> {
            log.trace("Running future: {}", task);
            try {
                runSingle(task);
                log.trace("Completed future: {}", task);
            } catch (Exception ex) {
                log.error("Failed future: {} {}", task, ex.getMessage());
            }
            return task;
        });

        return cf;
    }

    private Task fromBean(Bean<?> bean) {
        var ctx = bm.createCreationalContext(bean);
        try {
            var ref = bm.getReference(bean, bean.getBeanClass(), ctx);
            if (ref instanceof Task aTask) {
                return aTask;
            } else {
                log.error("Bean {} is not a Task", bean);
                throw new IllegalArgumentException("Bean is not a task");
            }
        }catch (Exception ex){
            ex.printStackTrace();
            log.error("Failed to create task from bean {}", bean, ex);
            throw ex;
        }
    }

    //TODO: Consider retries
    //TODO: Consider thread synchronization
    public void runSingle(Task task) {
        if (task == null) {
            log.error("Task is null");
            return;
        }
        history.add(task);
        try {
            task.setStartTime(LocalDateTime.now());
            //TODO: Asynchronous Execution
            checkInputs(task);
            task.apply();
            log.trace("Executed {}", task);
            //TODO: General waiter
        }catch (CapabilityNotFoundException e){
            var c = e.getCapability();
            log.warn("Capability not found: {}, try with -c '{}' or equivalent", c, c);
        }catch (ConfigurationNotFoundException e){
            var msg = e.getMessage();
            log.error("ERROR");
            log.warn("WARN");
            log.info("INFO");
            log.debug("DEBUG");
            log.trace("TRACE");
            // System.out.println("System.out");
            //TODO: Why dont you log this?
            System.out.println(msg);
            log.error("Configuration not found: {}", msg);
            log.warn("Expected configuration not found.");
            log.warn(e.getMessage());
        }catch (TaskFailedException e) {
            log.warn("Task failed to complete: {}", e.getMessage());
            //TODO: How to signal failure across tasks? See RetryTask
            throw new RuntimeException(e);
        }catch (Exception e) {
            task.getErrors().put(Message, e.getMessage());
            log.error("Error executing {}: {}", task, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            task.setEndTime(LocalDateTime.now());
        }
    }

    private void checkInputs(Task task) {
        List<Input> expected = task.getExpectedInputs();

        List<InputConfig> missing = new ArrayList<>();
        Map<Input, String> present = new HashMap<>();
        for(var inputKey:expected){
            var inputValue = task.input(inputKey)
                    .or( () -> fromConfig(inputKey));
            if(inputValue.isPresent()){
                present.put(inputKey, inputValue.get().toString());
            }else{
                missing.add(inputs.getConfig(inputKey));
            }
        }
        if (! expected.isEmpty())
            log.debug("[{}] {} inputs expected, {} present, {} missing", task.getName(), expected.size(), present.size(), missing.size());
        if (!missing.isEmpty()){
            log.debug("[{}] inputs are missing: {}", missing.size(), missing);
            throw new ConfigurationNotFoundException(missing);
        }
    }

    @SuppressWarnings("unused")
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @SuppressWarnings("unused")
    public String getStartTimeFmt() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startTime);
    }

    public Configuration getConfig() {
        return config;
    }


    private String executionId;

    public synchronized String getExecutionId() {
        if (executionId == null) {
            var sdf = new SimpleDateFormat("yyMMddHHmmss");
            executionId = "cj" + sdf.format(new Date());
        }
        return executionId;
    }

    public void setTask(String taskName) {
        this.task = taskName;
    }

    public void addCapability(String capability) {
        log.debug("Adding capability {}", capability);
        if ("all".equalsIgnoreCase(capability)) {
            var caps = List.of(Capabilities.values());
            capabilities.addAll(caps);
        } else if ("none".equalsIgnoreCase(capability)) {
            capabilities.clear();
        } else {
            try{
                var cap = Capabilities.valueOf(capability);
                capabilities.add(cap);
            } catch (IllegalArgumentException ex) {
                log.error("Invalid capability: {}", capability);
            }
        }
    }

    public Set<Capabilities> getCapabilities() {
        return capabilities;
    }

    public boolean hasCapabilities(Capabilities[] cs) {
        for (var c : cs) {
            if (!capabilities.contains(c)) {
                return false;
            }
        }
        return true;
    }

    public void addAll(List<String> capabilities) {
        capabilities.forEach(this::addCapability);
    }

    @Inject
    Inputs inputs;
    private Optional<?> fromConfig(Input input) {
        return Optional.ofNullable(inputs.getFromConfig(input));
    }


    @SuppressWarnings("unused")
    public void loadCapabilities(@Observes StartupEvent ev){
        getConfig().capabilities().ifPresent(this::addAll);
        log.debug("Loaded {} capabilities: {}",  capabilities.size(), capabilities);
    }
}
