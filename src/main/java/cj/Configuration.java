package cj;

import cj.aws.bypass.BypassTask;
import cj.qute.Templates;
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
import java.util.stream.Stream;

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


    public List<? extends Task> lookupTasks(String... prompt) {
        if (prompt == null || prompt.length == 0)
            return List.of();
        var taskName = prompt[0];
        var tasks = objects.createTasksByName(taskName);
        if (!tasks.isEmpty())
            return tasks;
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
        config.capabilities().ifPresent(this::addAll);
        log.debug("Loaded {} capabilities: {}", capabilities.size(), capabilities);
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
