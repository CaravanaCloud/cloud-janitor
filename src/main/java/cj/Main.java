package cj;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.annotations.StaticInitSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
@StaticInitSafe
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        initializeSystemProperties();
        execute(parse(args));
    }

    private static void initializeSystemProperties() {
        //TODO: Suppress quarkus printStartupTime
        System.setProperty("java.util.logging.manager","org.jboss.logmanager.LogManager");
    }

    private static String[] parse(String... args) {
        var parser = ArgsParser.of();
        var result = parser.parse(args);
        return result;
    }

    private static void execute(String... args) {
        Quarkus.run(CloudJanitor.class, args);
    }
}
