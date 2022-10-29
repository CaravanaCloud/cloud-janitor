package cj;
import cj.fs.FSUtils;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import org.slf4j.Logger;
import picocli.CommandLine;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@CommandLine.Command
public class Command implements Runnable, QuarkusApplication {
    @Inject
    CommandLine.IFactory factory;

    @Inject
    CloudJanitor cj;

    @Inject
    Logger log;

    public Command(){}

    @Override
    public void run() {
        log.trace("Command.run()");
        cj.run();
    }

    @Override
    public int run(String... args) throws Exception {
        log.trace("Command.run(...)");
        var exit = new CommandLine(this, factory).execute(args);
        return exit;
    }
}
