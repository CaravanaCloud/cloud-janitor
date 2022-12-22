package cj.aws.bypass;

import cj.BaseTask;
import cj.CJInput;
import cj.Input;
import cj.TaskConfiguration;
import cj.qute.Templates;
import cj.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static cj.StringUtils.join;

@Dependent
public class BypassTask extends BaseTask {

    @Override
    public void apply() {
        var prompt = inputList(CJInput.prompt, String.class);
        var task = bypass(prompt.toArray(new String[0]));
        task.ifPresent(this::submit);
    }

    private Optional<Task> bypass(String... args) {
        var enriched = enrich(args);
        if (enriched.isEmpty()) {
            debug("Empty bypass");
            return Optional.empty();
        }
        debug("Bypassing `{}` as `{}`", join(args), join(enriched));
        var enrichedArr = enriched.toArray(new String[0]);
        renderTemplates(args);
        var result = shell().shellTask(enrichedArr);
        return Optional.ofNullable(result);
    }

    private void renderTemplates(String... args) {
        var taskConfig = configuration().taskConfigForQuery(args);
        var templatesCfgs = taskConfig.map(c -> c.templates())
                .orElse(List.of());
        var taskName = args[0];
        try{
            //TODO: Change repeaters to pass identity and region inputs to tasks
            //TODO: Pass identity and region info to bypass template
            //TODO: Export identity and region environment variables to child shell tasks
            templatesCfgs.forEach(t -> {
                templates.render(taskName, t.template(), t.output());
            });
        }catch(Exception e){
            throw fail("Failed to render templates for task", e);
        }
    }

    private List<String> enrich(String... args) {
        if (args == null || args.length == 0) return List.of();
        var taskCfg = configuration().taskConfigForQuery(args);
        if (taskCfg.isEmpty()) return List.of(args);
        var taskName = taskCfg.get().name();
        var taskArgs = Arrays.copyOfRange(args, 1, args.length);
        var bypass = taskCfg.flatMap(TaskConfiguration::bypass);
        if (bypass.isEmpty()) return List.of(args);
        var bypassInputs = inputsMap().bypassInputs();
        var bypassList = bypass.get()
                .stream()
                .flatMap(expr -> bypassValues(expr, taskArgs))
                .toList();
        var result = bypassList;
        return result;
    }

    @Inject
    Templates templates;

    private Stream<String> bypassValues(String expr,
                                        String... prompt) {
        //TODO: Parse qute expressions
        var args = String.join(" ", prompt);
        Map<String,Object> data =  Map.of("args", args);
        var value = templates.fmt(expr, data);
        return Stream.of(value);
    }

    /*
    public void enrichBypass(String taskName, Input... input) {
        bypassMap.putAll(taskName, Arrays.asList(input));
    }
     */

}
