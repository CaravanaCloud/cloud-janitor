package cj;

import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Help {
    @Inject
    Tasks tasks;

    @Inject
    Configuration config;

    @Inject
    Logger log;

    public void showHelp() {
        var msg = new StringBuilder();
        msg.append("\n=== Cloud Janitor Help ===");

        var taskConfigs = tasks.findAll().stream();
        var configTask = config.task();
        if (configTask.isPresent()){
            var task = configTask.get();
            taskConfigs = taskConfigs.filter(t -> t.name().equals(task));
        } else {
            msg.append("\nHere are all the tasks you can run, using -t <task-name>, setting CJ_TASK or equivalent configuration.");
        }
        taskConfigs.forEach(tc -> this.showConfig(tc, msg));
        log.info(msg.toString());
    }

    private void showConfig(TaskConfiguration task, StringBuilder msg) {
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
    }
}
