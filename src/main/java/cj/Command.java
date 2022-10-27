package cj;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import picocli.CommandLine;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@CommandLine.Command
public class Command implements Runnable, QuarkusApplication {
    @Inject
    CommandLine.IFactory factory;

    @Inject
    CloudJanitor cj;

    public Command(){}

    @Override
    public void run() {
        cj.run();
    }

    @Override
    public int run(String... args) throws Exception {
        var exit = new CommandLine(this, factory).execute(args);
        return exit;
    }
}
