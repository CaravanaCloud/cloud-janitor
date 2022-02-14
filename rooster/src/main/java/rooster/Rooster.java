package rooster;

import org.kie.kogito.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.QuarkusApplication;
import rooster.spi.Flows;

public class Rooster implements QuarkusApplication{
    private static Logger log = LoggerFactory.getLogger(Rooster.class);

    @Override
    public int run(String... args) throws Exception {
        log.info("🐓");
        var providers = Flows.providers();
        while(providers.hasNext()){
            var flow = providers.next();
            flow.run();
        }
        log.info("🐔");
        return 0;
    }

}