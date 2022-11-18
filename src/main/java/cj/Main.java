package cj;


import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.annotations.StaticInitSafe;
import picocli.CommandLine;

@QuarkusMain
@StaticInitSafe
public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandJanitor()).execute(args);
        System.exit(exitCode);
    }
}
