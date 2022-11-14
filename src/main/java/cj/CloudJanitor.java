package cj;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

public class CloudJanitor {
    @Inject
    Logger log;

    @Inject
    Tasks tasks;

    @Inject
    LaunchMode launchMode;

    public int run(String task, List<String> inputs){
        log.trace("CloudJanitor.run()");
        try {
            tasks.run(task, inputs);
        } catch (Exception e) {
            log.error("CloudJanitor.run() failed", e);
            return -1;
        }
        return 0;
    }

    @SuppressWarnings("unused")
    void onStart(@Observes StartupEvent ev) {
        var execId = tasks.getExecutionId();
        log.info("Thank you for running cloud-janitor. This execution id is {}", execId);
        log.debug("Quarkus launch mode: {}", launchMode);
    }

    @SuppressWarnings("unused")
    void onStop(@Observes ShutdownEvent ev) {
        log.debug("Cloud Janitor stopped.");
    }

}