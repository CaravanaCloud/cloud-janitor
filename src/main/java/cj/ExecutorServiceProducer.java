package cj;

import io.quarkus.runtime.Startup;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Startup
public class ExecutorServiceProducer {
    private static ExecutorService pool;

    @Produces
    public synchronized ExecutorService newExecutor() {
        if (pool == null) {
            pool = Executors.newWorkStealingPool();
        }
        System.out.println("Asked for new ExecutorService, returning singleton.");
        return pool;
    }
}
