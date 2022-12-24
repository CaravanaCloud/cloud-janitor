package cj.qute;

import cj.*;
import cj.aws.AWSClientsManager;
import cj.aws.AWSIdentity;
import cj.fs.TaskFiles;
import cj.spi.Task;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Qute;
import io.quarkus.qute.Template;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class Templates implements Logging {
    static final String DEFAULT_PROFILE = "default";

    @Inject
    Engine engine;

    @Inject
    CJConfiguration config;

    @Inject
    InputsMap inputsMap;

    @Inject
    Tasks tasks;

    @Inject
    Shell shell;

    @Inject
    AWSClientsManager awsManager;
    private Map<String, String> templatePaths = new HashMap<>();

    public Template getTemplate(String location) {
        if (engine == null) {
            warn("Failed to inject template engine.");
            return null;
        }
        if (location == null) {
            error("Template requested for null location");
            throw new IllegalArgumentException("Template requested for null location");
        }
        try {
            return engine.getTemplate(location);
        } catch (Exception ex) {
            error("Failed to get template engine for {}", location);
            throw new RuntimeException(ex);
        }
    }

    public Path render(Task task, String template, String outputFileName) {
        return render(task.getName(), DEFAULT_PROFILE, template, outputFileName, Map.of());
    }

    public Path render(String taskName,
                       String template,
                       String outputFileName) {
        return render(taskName, DEFAULT_PROFILE , template, outputFileName, Map.of());
    }

    public Path render(String taskName,
                       String template,
                       String outputFileName,
                       Map<String, String> data) {
        return render(taskName, DEFAULT_PROFILE, template, outputFileName, data);
    }

    public Path render(Task task, String profile, String template, String outputFileName) {
        return render(task.getName(), profile, template, outputFileName, Map.of());
    }


    public Path render(String taskName,
                       String profile,
                       String template,
                       String outputFileName,
                       Map<String, String> data) {
        var location = "%s/%s/%s".formatted(
                taskName,
                profile,
                template
        );
        var content = renderString(taskName, location, data);
        if (content == null) return null;
        var outputFile = taskFile(taskName, outputFileName);
        debug("Rendering template {} to [{}] {}", template, content.length(), outputFile);
        TaskFiles.writeFile(outputFile, content);
        templatePaths.put(outputFile.toFile().getName(),
                outputFile.toAbsolutePath().toString());
        return outputFile;
    }

    public String render(Task task, String location, Map<String, String> inputs) {
        var taskInputs = getInputsMap(task);
        var merged = new HashMap<>(taskInputs);
        inputs.forEach((k,v) -> merged.merge(k, v, (a, b) -> b));
        return renderString(task.getName(), location, merged);
    }

    public String renderString(String taskName, String location, Map<String, String> inputs) {
        debug("Rendering template from {} with {} inputs", location, inputs.size());
        var template = getTemplate(location);
        if (template == null) {
            warn("Failed to load template from {}", location);
            throw new RuntimeException("Failed to load template from location "+location);
        }
        var data = new HashMap<String, Object>();
        data.putAll(inputs);
        data.put("config", config);
        try {
            var render = template
                    .data(data)
                    .render();
            return render;
        }catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to render template from "+ location);
        }
    }

    public Map<String, String> getInputsMap(Task task) {
        var result = new HashMap<String, String>();
        var expected = inputsMap.getExpectedInputs(task);
        expected.forEach(i -> {
            var in = task.input(i);
            if (in.isPresent()) {
                var val = in.get();
                putString(result, i, val);
            }
        });
        var actual = task.inputs();
        actual.forEach((i, o) -> putString(result, i, o));
        return result;
    }

    private void putString(HashMap<String, String> result, Input i, Object val) {
        if (val instanceof AWSIdentity id) {
            var info = awsManager.getInfo(id);
            var accountId = info.accountId();
            result.put("accountId", accountId);
        } else {
            result.put(i.toString(), val.toString());
        }
    }

    public String render(Task task, String location) {
        return render(task, location, Map.of());
    }

    public Path taskFile(Task task, String fileName) {
        return shell.taskFile(task, fileName);
    }

    public Path taskFile(String taskName, String fileName) {
        return shell.taskFile(taskName, fileName);
    }

    public String fmt(String expr, Map<String, Object> params) {
        var inputs = inputsMap.bypassInputs();
        var data = new HashMap<>(inputs);
        params.forEach((k, v) -> data.merge(k, v, (a, b) -> b));
        data.put("templatePath", templatePaths);
        try{
            var fmt = Qute.fmt(expr, data);
            return fmt;
        }catch (Exception ex){
            warn("Failed to format expression {}, omitting.", expr);
            return "";
        }
    }

    public Map<String, String> templatePaths() {
        return templatePaths;
    }
}
