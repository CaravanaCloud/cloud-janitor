package cj;

import io.quarkus.runtime.QuarkusApplication;
import org.slf4j.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.List;

@CommandLine.Command
public class CommandJanitor implements Runnable, QuarkusApplication {
    @Inject
    CommandLine.IFactory factory;

    @Inject
    CloudJanitor cj;

    @Inject
    Logger log;

    @CommandLine.Option(names = {"-t", "--task"}, description = "Task to be executed")
    String taskName;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Input parameter")
    List<String> input;

    public CommandJanitor(){}

    @Override
    public void run() {
        log.trace("Command.run()");
        cj.run(taskName, input);
    }

    @Override
    public int run(String... args) throws Exception {
        log.trace("Command.run(...)");
        var exit = new CommandLine(this, factory).execute(args);
        return exit;
    }
}