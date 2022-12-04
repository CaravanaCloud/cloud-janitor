package cj.qute;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


import cj.StartupObserver;
import io.quarkus.qute.Engine;
import io.quarkus.runtime.Startup;
import org.slf4j.Logger;

@SuppressWarnings("unused")
@Startup()
@ApplicationScoped
public class QuteStartup extends StartupObserver {
    @Inject
    Logger log;
    @Inject
    Engine engine;

    @Override
    public void onStart() {
        if (engine != null) {
            log.trace("Qute Engine checked OK");
            GlobalQuoteEngine.engine = engine;
        } else {
            throw new RuntimeException("Failed to initialize Qute Engine");
        }
    }

}
