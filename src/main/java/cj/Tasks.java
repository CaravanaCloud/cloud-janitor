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
import static com.google.common.base.Preconditions.checkArgument;

@ApplicationScoped
@Named("tasks")
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
    InputsMap inputsMap;

    @Inject
    Instance<ShellTask> shellInstance;

    @Inject
    Objects objects;

    @Inject
    Templates templates;

    Map<String, Map<OS, String[]>> installMap = new HashMap<>();

    Multimap<String, Input> bypassMap = ArrayListMultimap.create();

    public void run() {
        init();
        var tasks = lookupTasks();
        if (!tasks.isEmpty()) {
            runAll(tasks);
        } else if (config.bypass()) {
            bypass();
        }
        report();
    }

    private List<Task> lookupTasks() {
        var argsList = config.argsList();
        if (!argsList.isEmpty()) {
            var taskName = argsList.get(0);
            var tasks = lookupTasks(taskName);
            return tasks;
        }
        return List.of();
    }

    private void bypass() {
        var argsList = config.argsList();
        var enriched = enrich(argsList);
        log.debug("Bypassing `{}` as `{}`", join(argsList), join(enriched));
        if (enriched.isEmpty()) return;
        var enrichedArr = enriched.toArray(new String[enriched.size()]);
        exec(enrichedArr);
    }

    private String join(List<String> argsList) {
        return String.join(" ", argsList);
    }

    private List<String> enrich(List<String> args) {
        if (args.isEmpty()) return args;
        var taskName = args.get(0);
        var taskArgs = args.subList(1, args.size());
        var taskCfg = getTaskConfig(taskName);
        var bypass = taskCfg.flatMap(TaskConfiguration::bypass);
        var bypassList = bypass
                .map(xs -> xs.stream()
                        .flatMap(expr -> bypassValues(expr, taskArgs))
                        .toList());
        var result = bypassList.orElse(args);
        return result;
    }

    private Stream<String> bypassValues(String value, List<String> taskArgs) {
        if("{args}".equals(value)){
            return taskArgs.stream();
        }
        return Stream.of(value);
    }

    public Optional<TaskConfiguration> getTaskConfig(String taskName) {
        var taskCfgs = config.tasks();
        if (taskCfgs.isEmpty()) return Optional.empty();
        var taskCfg = taskCfgs.get()
                .stream()
                .filter(t -> t.name().equals(taskName))
                .findFirst();
        return taskCfg;
    }

    private Object valueOf(Input input) {
        var value = inputsMap.valueOf(input);
        return value;
    }

    public Stream<Input> bypassInputs(String taskName) {
        return bypassMap.get(taskName).stream();
    }

    private void init() {
        log.trace("Tasks.run()");
        log.debug("Capabilities: {}", getCapabilities());
        log.debug("Parallel: {}", config.parallel());
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
            var prefix = getConfig().namingPrefix().orElse("");
            var pattern = getConfig().timestampPattern();
            var sdf = new SimpleDateFormat(pattern);
            executionId = prefix + sdf.format(new Date());
        }
        return executionId;
    }

    public void setTask(String taskName) {
        this.task = taskName;
    }

    public void addCapability(String capability) {
        log.trace("Adding capability: {}", capability);
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

    public boolean hasCapabilities(Capabilities... cs) {
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
        List<Input> expected = inputsMap.getExpectedInputs(task);

        List<InputConfig> missing = new ArrayList<>();
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

    public ExecResult exec(String... cmdArgs) {
        return exec(false, cmdArgs);
    }

    @SuppressWarnings("unused")
    protected ExecResult exec(Boolean dryRun, String... cmdArgs) {
        return exec(getConfig().execTimeout(), dryRun, cmdArgs);
    }

    @SuppressWarnings("all")
    public ExecResult exec(Long timeout, String... cmdArgs) {
        return exec(timeout, false, cmdArgs);
    }

    public ExecResult exec(Long timeoutMins, Boolean isDryRun, String... cmdArgs) {
        if (cmdArgs.length == 1) {
            var cmd = cmdArgs[0];
            if (cmd.contains(" ")) {
                cmdArgs = cmd.split(" ");
            }
        }
        if (timeoutMins == null) {
            timeoutMins = getConfig().execTimeout();
        }
        var shellTask = shellTask(isDryRun, cmdArgs)
                .withInput(ShellInput.timeout, timeoutMins);
        submit(shellTask);
        @SuppressWarnings("all")
        var stdout = shellTask.outputString(ShellOutput.stdout);
        checkArgument(stdout.isPresent(), "No stdout from shell task");
        var stderr = shellTask.outputString(ShellOutput.stderr);
        checkArgument(stderr.isPresent(), "No stderr from shell task");
        var exitCode = shellTask.outputAs(ShellOutput.exitCode, Integer.class);
        checkArgument(exitCode.isPresent(), "No exit code from shell task");
        return new ExecResult(exitCode.get(), stdout.get(), stderr.get());
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

    public List<TaskConfiguration> findAll() {
        @SuppressWarnings("redundant")
        var tasks = bm.getBeans(Task.class)
                .stream()
                .map(objects::configFromBean)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(TaskConfiguration::name))
                .toList();
        return tasks;
    }

    public String generateResourceName() {
        return getExecutionId();
    }


    public void shell(String[] cmd) {
        checkArgument(cmd.length > 0, "No command provided");
        var binary = cmd[0];
        checkCmd(binary, installMap.get(binary));
        exec(cmd);
    }

    public void mapInstall(String binary, Map<OS, String[]> commands) {
        installMap.put(binary, commands);
    }

    public void enrichBypass(String taskName, Input... input) {
        bypassMap.putAll(taskName, Arrays.asList(input));
    }

    public Path taskFile(Task task, String fileName) {
        return taskFile(task.getPathName(), fileName);
    }

    public Path taskFile(String taskName, String fileName) {
        return FSUtils.taskDir(taskName).resolve(fileName);
    }
}