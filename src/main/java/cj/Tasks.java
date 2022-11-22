package cj;

import cj.ocp.CapabilityNotFoundException;
import cj.reporting.Reporting;
import cj.shell.CheckShellCommandExistsTask;
import cj.shell.ShellInput;
import cj.shell.ShellTask;
import cj.spi.Task;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


import static cj.Errors.Type.Message;

@ApplicationScoped
public class Tasks {
    static final LocalDateTime startTime = LocalDateTime.now();

    // @Inject
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

    private String executionId;

    @Inject
    Inputs inputs;

    @Inject
    Instance<ShellTask> shellInstance;

    @Inject
    Beans beans;

    public void run() {
        log.trace("Tasks.run()");
        log.debug("Capabilities: {}", getCapabilities());
        log.debug("Parallel: {}", config.parallel());

        var task = config.task();
        task.ifPresent(this::run);
        var tasks = config.tasks();
        tasks.ifPresent(ts -> ts.forEach(this::run));
        report();
    }

    // TODO: Consider async execution
    public Task submit(Task task) {
        try {
            submitNow(task);
            return task;
        } catch (Exception e) {
            log.info("Exception running task {}: {}", task, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    public Task submitNow(Task task) {
        var thisInputs = task.getInputs();
        var dependencies = task.getDependencies();
        dependencies.forEach(d -> {
            // TODO: Consider if dependencies should inherit inputs
            d.getInputs().putAll(thisInputs);
            submit(d);
        });
        runSingle(task);
        return task;
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
            task.getErrors().put(Message, e.getMessage());
            log.error("Error executing {}: {}", task, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            task.setEndTime(LocalDateTime.now());
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
            try {
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

    @SuppressWarnings("unused")
    public void loadCapabilities(@Observes StartupEvent ev) {
        getConfig().capabilities().ifPresent(this::addAll);
        log.debug("Loaded {} capabilities: {}", capabilities.size(), capabilities);
    }

    private void run(String taskName) {
        log.info("Running task: {}", taskName);
        var matches = lookupTasks(taskName);
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
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Failed to create task from bean {}", bean, ex);
            throw ex;
        }
    }

    private void checkInputs(Task task) {
        List<Input> expected = inputs.getExpectedInputs(task);

        List<InputConfig> missing = new ArrayList<>();
        Map<Input, String> present = new HashMap<>();
        for (var inputKey : expected) {
            var inputValue = task.input(inputKey)
                    .or(() -> fromConfig(inputKey));
            if (inputValue.isPresent()) {
                present.put(inputKey, inputValue.get().toString());
            } else {
                missing.add(inputs.getConfig(inputKey));
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
        return Optional.ofNullable(inputs.getFromConfig(input));
    }

    public Optional<String> exec(String... cmdArgs) {
        return exec(ShellInput.DEFAULT_TIMEOUT_MINS, false, cmdArgs);
    }

    @SuppressWarnings("unused")
    protected Optional<String> exec(Boolean dryRun, String... cmdArgs) {
        return exec(ShellInput.DEFAULT_TIMEOUT_MINS, dryRun, cmdArgs);
    }

    @SuppressWarnings("all")
    public Optional<String> exec(Long timeout, String... cmdArgs) {
        return exec(timeout, false, cmdArgs);
    }

    public Optional<String> exec(Long timeoutMins, Boolean isDryRun, String... cmdArgs) {
        if (cmdArgs.length == 1) {
            var cmd = cmdArgs[0];
            if (cmd.contains(" ")) {
                cmdArgs = cmd.split(" ");
            }
        }
        var shellTask = shellTask(isDryRun, cmdArgs)
                .withInput(ShellInput.timeout, timeoutMins);
        submit(shellTask);
        @SuppressWarnings("all")
        var output = shellTask.outputString(Output.shell.stdout);
        return output;
    }

    public ShellTask shellTask(List<String> cmdsList) {
        String[] cmdArgs = cmdsList.toArray(String[]::new);
        return shellTask(false, cmdArgs);
    }

    public ShellTask shellTask(String... cmdArgs) {
        return shellTask(false, cmdArgs);
    }

    public ShellTask shellTask(Boolean isDryRun, String... cmdArgs) {
        var cmdList = Arrays.asList(cmdArgs);
        var shellTask = shellInstance.get();
        if (isDryRun != null) {
            shellTask.withInput(ShellInput.dryRun, isDryRun);
        }
        shellTask.withInput(ShellInput.cmds, cmdList);
        return shellTask;
    }

    @Inject
    Instance<CheckShellCommandExistsTask> checkCmd;

    @Inject
    Instance<RetryTask> retry;

    @SuppressWarnings("UnusedReturnValue")
    public Task checkCmd(String executable, Map<OS, String[]> fixMap) {
        var checkTask = checkCmd.get().withInput(ShellInput.cmd, executable);
        var installTask = shellTask(OS.get(fixMap));
        return retry(checkTask, installTask);
    }
    public Task retry(Task theMainTask, Task theFixTask) {
      var retryTask = retry.get()
        .withInput(CJInput.task, theMainTask)
        .withInput(CJInput.fixTask, theFixTask);
      return submit(retryTask);
     }
     public List<TaskConfiguration> findAll(){
        @SuppressWarnings("redundant")
        var tasks = bm.getBeans(Task.class)
                .stream()
                .map(beans::configFromBean)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TaskConfiguration::name))
                .toList();
        return tasks;
     }


}
