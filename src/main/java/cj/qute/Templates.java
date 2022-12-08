package cj.qute;

import cj.*;
import cj.aws.AWSIdentity;
import cj.fs.FSUtils;
import cj.spi.Task;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class Templates implements Logging {
    static final String DEFAULT_PROFILE = "default";

    @Inject
    Engine engine;

    @Inject
    Configuration config;

    @Inject
    InputsMap inputsMap;


    public Template getTemplate(String location) {
        if (engine == null) {
            warn("Failed to inject template engine, trying global engine.");
            engine = GlobalQuoteEngine.engine;
            if (engine == null) {
                throw new RuntimeException("Failed to resolve template engine");
            }
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

    public Path taskDir(Task task) {
        return FSUtils.taskDir(task);
    }

    public Path render(Task task, String template, String outputFileName) {
        return render(task, DEFAULT_PROFILE, template, taskFile(task, outputFileName));
    }

    public Path render(Task task, String profile, String template, String outputFileName) {
        return render(task, profile, template, taskFile(task, outputFileName));
    }

    public Path render(Task task, String profile, String template, Path outputFile) {
        var location = "%s/%s/%s".formatted(
                task.getName(),
                profile,
                template
        );
        var content = render(task, location, Map.of());
        debug("Rendering template {} to [{}] {}", template, content.length(), outputFile);
        FSUtils.writeFile(outputFile, content);
        return outputFile;
    }

    public String render(Task task, String location, Map<String, String> inputs) {
        debug("Rendering template from {} with {} inputs", location, inputs.size());
        var installConfigTemplate = getTemplate(location);
        if (installConfigTemplate == null) {
            warn("Failed to load template from {}", location);
            throw new RuntimeException("Failed to load template from %s".formatted(location));
        }
        var data = new HashMap<String, Object>();
        data.putAll(inputs);
        data.putAll(getInputsMap(task));
        data.put("config", config);
        @SuppressWarnings("redundant")
        var render = installConfigTemplate
                .data(data)
                .render();
        return render;
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
        var actual = task.getInputs();
        actual.forEach((i, o) -> {
            putString(result, i, o);
        });
        return result;
    }

    private void putString(HashMap<String, String> result, Input i, Object val) {
        if (val instanceof AWSIdentity id) {
            result.put("accountId", id.accountId());
        } else {
            result.put(i.toString(), val.toString());
        }
    }

    public String render(Task task, String location) {
        return render(task, location, Map.of());
    }

    public Path taskFile(Task task, String fileName) {
        return taskDir(task).resolve(fileName);
    }
}
