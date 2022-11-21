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
<<<<<<< HEAD
=======
    @Inject
    Logger log;
>>>>>>> 7b1c21a (1.3.7 - Improved initialization)
    private static ExecutorService pool;

    @Produces
    public synchronized ExecutorService newExecutor() {
        if (pool == null) {
            pool = Executors.newWorkStealingPool();
        }
<<<<<<< HEAD
        System.out.println("Asked for new ExecutorService, returning singleton.");
=======
        log.debug("Asked for new ExecutorService, returning singleton.");
>>>>>>> 7b1c21a (1.3.7 - Improved initialization)
        return pool;
    }
}
