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
        String msg = "\n== Task " +
                "\nname: %s".formatted(task.name()) +
                "\ndescription: %s".formatted(task.description()) +
                "\nmaturity: %s".formatted(task.maturityLevel());
        log.info(msg);
    }
}
