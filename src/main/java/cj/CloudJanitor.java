package cj;

import io.quarkus.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class CloudJanitor implements QuarkusApplication{
    private static final Logger log = LoggerFactory.getLogger(CloudJanitor.class);

    @Inject
    Tasks tasks;

    @Inject
    LaunchMode launchMode;

    @Override
    public int run(String... args){
        tasks.run(args);
        return 0;
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("Cloud Janitor is starting.");
        log.info("Launch mode: {}", launchMode.name());
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.debug("Cloud Janitor is stopping...");
    }
}