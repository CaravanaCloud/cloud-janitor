package cj;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.StaticInitSafe;
import org.slf4j.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@CommandLine.Command(version = "1.3.4",
        mixinStandardHelpOptions = true,
        name = "cloud-janitor", description = "Cloud Janitor at your service.")
@StaticInitSafe
public class CommandJanitor implements Runnable,
        QuarkusApplication {
    @Inject
    CommandLine.IFactory factory;

    @Inject
    CloudJanitor cj;
    @Inject
    Tasks tasks;

    @Inject
    Logger log;

    @CommandLine.Option(names = {"-t", "--task"}, description = "Task to be executed. Try '-t hello'")
    Optional<String> taskName;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Input parameters for the task, repeatable.")
    List<String> input;

    @CommandLine.Option(names = {"-c", "--capabilities"},
            defaultValue = "none",
            description = "Feature toggles (try -c 'all').")
    List<String> capabilities;

    public CommandJanitor(){}

    @Override
    public void run() {
        log.trace("Command.run()");
        parseArgs();
        cj.run();
    }

    private void parseArgs() {
        taskName.ifPresent(tasks::setTask);
        if (input != null)
            input.forEach(tasks::addInput);
        capabilities.forEach(tasks::addCapability);
    }

    @Override
    public int run(String... args) throws Exception {
        log.trace("Command.run(...)");
        var exit = new CommandLine(this, factory).execute(args);
        return exit;
    }

}