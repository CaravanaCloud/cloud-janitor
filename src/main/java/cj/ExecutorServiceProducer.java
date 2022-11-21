package cj;

import io.quarkus.runtime.Startup;
import org.slf4j.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Startup
public class ExecutorServiceProducer {
    @Inject
    Logger log;
    private static ExecutorService pool;

    @Produces
    public synchronized ExecutorService newExecutor() {
        if (pool == null) {
            pool = Executors.newWorkStealingPool();
        }
        log.debug("Asked for new ExecutorService, returning singleton.");
        return pool;
    }
}
