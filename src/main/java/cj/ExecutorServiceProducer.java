package cj;

import io.quarkus.runtime.Startup;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Startup
public class ExecutorServiceProducer {
    @Inject
    Logger log;

    @Produces
    public synchronized ExecutorService newExecutor() {
        log.trace("Producing new ExecutorService");
        return Executors.newWorkStealingPool();
    }
}
