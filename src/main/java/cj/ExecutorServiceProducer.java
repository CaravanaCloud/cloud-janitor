package cj;

import io.quarkus.runtime.Startup;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
@Startup
@SuppressWarnings("unused")
public class ExecutorServiceProducer {
    @Produces
    public ExecutorService newExecutor()
    {
        return java.util.concurrent.Executors.newWorkStealingPool();
    }
}
