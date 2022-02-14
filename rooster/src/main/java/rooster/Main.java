package rooster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain    
public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        log.info("🤠");
        Quarkus.run(Rooster.class, args);
    }
}

