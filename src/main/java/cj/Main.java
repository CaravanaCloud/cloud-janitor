package cj;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.annotations.StaticInitSafe;
import picocli.CommandLine;

@QuarkusMain
@StaticInitSafe
public class Main {
    public static void main(String[] args) {
        var janitor = CommandJanitor.of();
        var command = janitor.commandLine();
        parseArgs(janitor, command,  args);
        execute(command, args);
    }

    private static void execute(CommandLine command, String[] args) {
        int exitCode = command.execute(args);
        if (exitCode == 0) {
            System.out.println("[Main] Command line parsed.");
            Quarkus.run(CloudJanitor.class, args);
            System.out.println("[Main] Quarkus run() returned, exiting application.");
        } else {
            System.out.println("[Main] Command line failed to parse.");
        }
    }

    private static void parseArgs(
            CommandJanitor janitor,
            CommandLine command,
            String... args) {
        command.parseArgs(args);
        var isVersionRequested = command.isVersionHelpRequested();
        if (isVersionRequested){
            System.setProperty("cj.showVersion", "true");
        }
        var isHelpRequested = command.isUsageHelpRequested();
        if (isHelpRequested){
            System.setProperty("cj.showHelp", "true");
            janitor.parseArgs();
        }
    }
}
