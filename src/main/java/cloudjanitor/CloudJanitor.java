package cloudjanitor;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.QuarkusApplication;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class CloudJanitor implements QuarkusApplication{
    private static final Logger log = LoggerFactory.getLogger(CloudJanitor.class);

    @Inject
    Tasks tasks;

    @Override
    public int run(String... args){
        tasks.run(args);
        return 0;
    }


    void onStart(@Observes StartupEvent ev) {
        log.debug("Cloud Janitor is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.debug("Cloud Janitor is stopping...");
    }
}