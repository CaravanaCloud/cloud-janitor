package cj;

import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Help {
    @Inject
    Tasks tasks;

    @Inject
    Logger log;

    public void showHelp() {
        log.info("=== Cloud Janitor Help ===");
        var taskConfigs = tasks.findAll();
        taskConfigs.forEach(this::showConfig);
    }

    private void showConfig(TaskConfiguration task) {
        StringBuilder msg = new StringBuilder();
        msg.append("\n== Task ");
        msg.append("\nname: %s".formatted(task.name()));
        msg.append("\ndescription: %s".formatted(task.description()));
        msg.append("\nmaturity: %s".formatted(task.maturityLevel()));
        task.inputs().forEach(input -> {
            msg.append("\n    input: %s".formatted(input.input().toString()));
            msg.append("\n  yml key: %s".formatted(input.configKey()));
            msg.append("\n  env var: %s".formatted(input.getEnvVarName()));
            msg.append("\n");
        });
        log.info(msg.toString());
    }
}
