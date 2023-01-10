package cj;

import cj.aws.bypass.BypassTask;
import cj.spi.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

import static cj.StringUtils.join;

@ApplicationScoped
public class Configuration {
    private final Multimap<String, Input> bypassMap = ArrayListMultimap.create();
    private final Set<Capabilities> capabilities = new HashSet<>();
    @Inject
    Logger log;
    @Inject
    CJConfiguration config;
    @Inject
    Objects objects;
    @Inject
    Shell shell;
    private String executionId;
    @Inject
    InputsMap inputsMap;
    @Inject
    Instance<BypassTask> bypassInstance;


    public List<? extends Task> lookupTasks(final String... prompt) {
        if (prompt == null || prompt.length == 0)
            return List.of();
        if (prompt.length == 1 && prompt[0].contains(" "))
            return lookupTasks(prompt[0].split(" "));

        var cfg = taskConfigForQuery(prompt)
                .or(() -> TaskConfigurationRecord.of(prompt));
        if (cfg.isEmpty())
            log.warn("No task found for prompt: {}", prompt);
        var taskCfg = cfg.get();
        List<? extends Task> result =  List.of();
        if (taskCfg.bypass().isPresent()) {
            result = lookupBypass(prompt);
        }else if (! taskCfg.steps().isEmpty()) {
            result = lookupSteps(prompt, taskCfg);
        }else {
            result = lookupByName(prompt, taskCfg);
        }
        if (result.isEmpty() && config.bypass()){
            result = lookupBypass(prompt);
        }
        if(result.isEmpty()){
            log.warn("No task found for prompt: {}", prompt);
        }
        return result;
    }

    private List<? extends Task> lookupByName(String[] prompt, TaskConfiguration taskCfg) {
        var tasks = objects.createTasksByName(taskCfg.name());
        return tasks;
    }

    private List<? extends Task> lookupSteps(String[] prompt, TaskConfiguration taskCfg) {
        var steps = taskCfg.steps();
        var ts = new ArrayList<Task>();
        for (var step : steps) {
            var runStep = step.run();
            if (runStep.isPresent()){
                var run = runStep.get();
                var stepTasks = lookupTasks(run);
                ts.addAll(stepTasks);
            }
        }
        return ts;
    }

    private List<? extends Task> lookupBypass(String[] prompt) {
        if (config.bypass())
            return List.of(bypassInstance
                    .get()
                    .withInput(CJInput.prompt, Arrays.asList(prompt)));
        return List.of();
    }


    public List<TaskConfiguration> taskConfigs() {
        return objects.allTaskConfigurations();
    }

    public Optional<TaskConfiguration> taskConfigForQuery(List<String> query) {
        return taskConfigForQuery(query.toArray(new String[0]));
    }

    public Optional<TaskConfiguration> taskConfigForQuery(String... query) {
        if (query == null || query.length == 0) return Optional.empty();
        var taskCfgs = taskConfigs();
        if (taskCfgs.isEmpty()) return Optional.empty();
        var taskName = query[0];
        var taskCfg = taskCfgs
                .stream()
                .filter(t -> t.name().equals(taskName))
                .findFirst();
        return taskCfg;
    }

    @Override
    public String toString() {
        return Map.of(
                "executionId", getExecutionId(),
                "os", OS.of(),
                "arch", Arch.of(),
                "capabilities", capabilities,
                "parallel", config.parallel()
        ).toString();
    }

    public Set<Capabilities> getCapabilities() {
        return capabilities;
    }

    public String getExecutionId() {
        if (executionId == null) {
            var prefix = config.namingPrefix().orElse("");
            var pattern = config.timestampPattern();
            var sdf = new SimpleDateFormat(pattern);
            executionId = prefix + sdf.format(new Date());
        }
        return executionId;
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
        config.capabilities().ifPresentOrElse(this::addAll,
                this::addDefaultCapabilities);
        log.debug("Loaded {} capabilities: {}", capabilities.size(), capabilities);
    }

    private void addDefaultCapabilities() {
        capabilities.add(Capabilities.LOCAL_SHELL);
    }

    public boolean reportEnabled() {
        return config.report().enabled();
    }


    public boolean parallel() {
        return config.parallel();
    }

    public long checkpointSleep() {
        return config.checkpointSleep();
    }


    private String namingSeparator() {
        return config.namingSeparator().orElse("-");
    }

    private String altSeparator() {
        return config.altSeparator().orElse("_");
    }

    private String compose(String separator, String altSeparator, String[] context) {
        var name = composeNameSep(separator, context);
        name = name.replaceAll(altSeparator, separator);
        name = name.toLowerCase();
        return name;
    }

    protected String composeNameSep(String separator, String... context) {
        var prefix = config.namingPrefix().orElse("");
        var result = prefix
                + separator
                + String.join(separator, context);
        return result;
    }

    public String composeName(String... tokens) {
        return compose(namingSeparator(), altSeparator(), tokens);
    }

    public String composeNameAlt(String... tokens) {
        return compose(altSeparator(), namingSeparator(), tokens);
    }


    public Long execTimeout() {
        return config.execTimeout();
    }

    public String consoleLevel() {
        return config.consoleLevel();
    }

    public Optional<String> helpTask() {
        return config.task();
    }

    public CJConfiguration raw() {
        return config;
    }


    public Optional<TaskConfiguration> taskConfigForTask(Task task) {
        var taskName = task.getName();
        var taskCfg = taskConfigForQuery(taskName);
        return taskCfg;
    }
}
