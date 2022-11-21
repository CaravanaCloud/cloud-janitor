package cj.qute;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.qute.Engine;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;

@Startup
@ApplicationScoped
public class QuteStartupObserver {
    @Inject
    Logger log;
    @Inject
    Engine engine;

    public void checkEngine(@Observes StartupEvent ev) {
        if (engine != null) {
            log.debug("Qute Engine checked OK");
            GlobalQuoteEngine.engine = engine;
        } else {
            throw new RuntimeException("Failed to initialize Qute Engine");
        }
    }

}
