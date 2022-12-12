package cj;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class CloudJanitor implements QuarkusApplication {
    public static final String VERSION = "1.7.3";
    @Inject
    Logger log;

    @Inject
    Tasks tasks;

    @Inject
    Configuration config;

    @Inject
    LaunchMode launchMode;

    @Inject
    Instance<Help> help;

    @Override
    public int run(String... args) throws Exception {
        log.trace("CloudJanitor.run(...)");
        try {
            if (config.showHelp()){
                showHelp();
            } else if(config.showVersion()){
                showVersion();
            } else {
                tasks.run(args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("CloudJanitor.run() failed", e);
            return -1;
        }
        return 0;
    }

    private void showVersion() {
        log.info(CloudJanitor.VERSION);
    }

    private void showHelp() {
        help.get().showHelp();
    }

    @SuppressWarnings("unused")
    void onStart(@Observes StartupEvent ev) {
        var execId = tasks.getExecutionId();
        log.info("Thank you for running cloud-janitor.");
        log.debug("This execution id is {}", execId);
        log.debug("Quarkus launch mode: {}", launchMode);
        log.trace("Startup Event {}", ev);
    }

    @SuppressWarnings("unused")
    void onStop(@Observes ShutdownEvent ev) {
        log.debug("Cloud Janitor stopped.");
    }

}