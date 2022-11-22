package cj;

import cj.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(version = "1.4.0", mixinStandardHelpOptions = true, name = "cloud-janitor", description = "Cloud Janitor at your service.")
public class CommandJanitor implements Callable<Integer> {
    private static final Logger log = LoggerFactory.getLogger(CommandJanitor.class);

    @SuppressWarnings("all")
    @CommandLine.Option(names = { "-t", "--task" }, description = "task to be executed. Try '-t hello'")
    String taskName;

    @CommandLine.Option(names = { "-i", "--input" }, description = "input parameters for the task, repeatable.")
    List<String> input;

    @CommandLine.Option(names = { "-c", "--capabilities" }, description = "feature toggles (try -c 'all').")
    String capabilities;

    @CommandLine.Option(names = { "-l", "--log-level" }, description = "log level (try -l 'debug').")
    String logLevel;

    @CommandLine.Option(names = { "-v", "--version" }, description = "show version and exit.")
    Boolean showVersion;

    @CommandLine.Option(names = { "-h", "--help" }, description = "show help and exit.")
    Boolean showHelp;

    public CommandJanitor() {
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public Integer call() throws Exception {
        parseArgs();
        return 0;
    }

    private void parseArgs() {
        trySetProperty("quarkus.log.console.level", logLevel);
        trySetProperty("cj.task", taskName);
        trySetProperty("cj.capabilities", capabilities);
        trySetProperty("cj.showVersion", showVersion);
        trySetProperty("cj.showHelp", showHelp);
        if (input != null)
            for (var i : input) {
                trySetProperty(i, i);
            }
        loadLocalQuarkusConfig();
    }

    private void trySetProperty(String systemProperty, Boolean argument) {
        if (argument != null) {
            log.debug("{} := {}", systemProperty, argument);
            System.setProperty(systemProperty, argument.toString());
        }
    }

    private void trySetProperty(String systemProperty, String argument) {
        if (argument != null) {
            log.debug("{} := {}", systemProperty, argument);
            System.setProperty(systemProperty, argument);
        }
    }

    private static void loadLocalQuarkusConfig() {
        var localConfigDir = FSUtils.getLocalConfigDir();
        var localConfigFile = localConfigDir.resolve("application.yaml");
        if (localConfigFile.toFile().exists()) {
            var configLocation = localConfigDir.toAbsolutePath().toString();
            // System.out.println("Local configuration file found at " + configLocation);
            System.setProperty("quarkus.config.locations", configLocation);
        }
    }

}