package cj;

import cj.spi.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static cj.StringUtils.join;

@ApplicationScoped
public class Configuration {
    @Inject
    Logger log;

    @Inject
    CJConfiguration config;

    @Inject
    Objects objects;

    @Inject
    Shell shell;


    private Multimap<String, Input> bypassMap = ArrayListMultimap.create();
    private Set<Capabilities> capabilities = new HashSet<>();

    private String executionId;


    public List<? extends Task> lookupTasks(String[] args) {
        var tasks = Stream.concat(
                tasksFromArgs(args).stream(),
                tasksFromConfig().stream()
        ).toList();
        return tasks;
    }

    private List<? extends Task> tasksFromConfig() {
        var tasks = config.task()
                .map(this::createTasks)
                .orElse(List.of());
        return tasks;
    }

    private List<? extends Task> tasksFromArgs(String[] args) {
        var tasks = createTasks(args);
        return tasks;
    }

    public List<TaskConfiguration> taskConfigs() {
        //TODO: Also load task configs fom java annotations
        var result = config.tasks()
                .orElse(List.of());
        return result;
    }

    public Optional<TaskConfiguration> taskConfigByName(String taskName) {
        var taskCfgs = taskConfigs();
        if (taskCfgs.isEmpty()) return Optional.empty();
        var taskCfg = taskCfgs
                .stream()
                .filter(t -> t.equals(taskName))
                .findFirst();
        return taskCfg;
    }

    private List<? extends Task> createTasks(String... args) {
        if (args == null || args.length == 0)
            return List.of();
        var taskName = args[0];
        var tasks = objects.createTasksByName(taskName);
        if (!tasks.isEmpty())
            return tasks;
        if (config.bypass())
            return bypass(args);
        return List.of();
    }

    private List<? extends Task> bypass(String[] args) {
        var argsList = List.of(args);
        var enriched = enrich(argsList);
        if (enriched.isEmpty()) {
            log.debug("Empty bypass");
            return List.of();
        }
        log.debug("Bypassing `{}` as `{}`", join(argsList), join(enriched));
        var enrichedArr = enriched.toArray(new String[enriched.size()]);
        var result = List.of(shell.shellTask(enrichedArr));
        return result;
    }

    private List<String> enrich(List<String> args) {
        if (args.isEmpty()) return args;
        var taskName = args.get(0);
        var taskArgs = args.subList(1, args.size());
        var taskCfg = taskConfigByName(taskName);
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
    public void enrichBypass(String taskName, Input... input) {
        bypassMap.putAll(taskName, Arrays.asList(input));
    }

    @Override
    public String toString() {
        var buf = new StringBuffer();
        buf.append("Configuration{");
        buf.append("bypassMap=").append(bypassMap);
        buf.append(", capabilities=").append(capabilities);
        buf.append(", executionId='").append(executionId).append('\'');
        buf.append(", paralel=").append(config.parallel());
        buf.append('}');
        return buf.toString();
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
        return compose(altSeparator(), namingSeparator(),  tokens);
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

    public CJConfiguration raw(){
        return config;
    }
}
