package cj;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.annotations.StaticInitSafe;
import picocli.CommandLine;

@QuarkusMain
@StaticInitSafe
public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandJanitor()).execute(args);
        if (exitCode == 0) {
            System.out.println("[Main] Command line parsed.");
            Quarkus.run(CloudJanitor.class, args);
            System.out.println("[Main] Quarkus run() returned, exiting main().");
        } else {
            System.out.println("[Main] Command line failed to parse.");
        }
    }
}
