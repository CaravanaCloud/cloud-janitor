package cj;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.StaticInitSafe;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import picocli.CommandLine;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    Logger log;

    @CommandLine.Option(names = {"-t", "--task"}, description = "Task to be executed. Try '-t hello'")
    String taskName;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Input parameters for the task, repeatable.")
    List<String> input;

    @CommandLine.Option(names = {"-d", "--dry-run"}, description = "Disable dry-run safety check.")
    Boolean dryRun;

    public CommandJanitor(){}



    @Override
    public void run() {
        log.trace("Command.run()");
        InMemoryConfigSource.configuration.put("cj.dryRun", dryRun.toString());

        cj.run(taskName, input);
    }

    @Override
    public int run(String... args) throws Exception {
        log.trace("Command.run(...)");
        var exit = new CommandLine(this, factory).execute(args);
        return exit;
    }

}