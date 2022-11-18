package cj;

import cj.fs.FSUtils;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(version = "1.3.6",
        mixinStandardHelpOptions = true,
        name = "cloud-janitor", description = "Cloud Janitor at your service.")
public class CommandJanitor implements Callable<Integer>, QuarkusApplication {
    private static final Logger log = LoggerFactory.getLogger(CommandJanitor.class);
    @Inject
    CloudJanitor cj;


    @SuppressWarnings("all")
    @CommandLine.Option(names = {"-t", "--task"}, description = "Task to be executed. Try '-t hello'")
    String taskName;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Input parameters for the task, repeatable.")
    List<String> input;

    @CommandLine.Option(names = {"-c", "--capabilities"}, description = "Feature toggles (try -c 'all').")
    String capabilities;

    @CommandLine.Option(names = {"-l", "--log-level"}, description = "log level (try -l 'debug').")
    String logLevel;

    public CommandJanitor(){}

    @Override
    public Integer call() throws Exception {
        parseArgs();
        Quarkus.run(CommandJanitor.class);
        return 0;
    }

    private void parseArgs() {
        trySetProperty("quarkus.log.console.level", logLevel);
        trySetProperty("cj.task", taskName);
        trySetProperty("cj.capabilities", capabilities);
        if (input != null) for(var i:input){
            trySetProperty(i, i);
        }
        loadLocalQuarkusConfig();
    }

    private void trySetProperty(String systemProperty, String argument) {
        if(argument != null){
            log.debug("{} := {}", systemProperty, argument);
            System.setProperty(systemProperty, argument);
        }
    }

    @Override
    public int run(String... args) throws Exception {
        log.trace("Command.run(...)");
        cj.run();
        return 0;
    }

    private static void loadLocalQuarkusConfig() {
        var localConfigDir = FSUtils.getLocalConfigDir();
        var localConfigFile = localConfigDir.resolve("application.yaml");
        if (localConfigFile.toFile().exists()){
            var configLocation = localConfigDir.toAbsolutePath().toString();
            //System.out.println("Local configuration file found at " + configLocation);
            System.setProperty("quarkus.config.locations", configLocation);
        }
    }

}