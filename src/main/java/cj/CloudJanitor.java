package cj;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import io.quarkus.runtime.QuarkusApplication;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class CloudJanitor implements QuarkusApplication {
    @Inject
    Logger log;

    @Inject
    Tasks tasks;

    @Inject
    LaunchMode launchMode;

    @Inject
    ExecutorService executor;

    @Override
    public int run(String... args) throws Exception {
        log.trace("CloudJanitor.run(...)");
        try {
            tasks.run();
//            log.debug("Quarkus wait for exit...");
//            Quarkus.waitForExit();
        } catch (Exception e) {
            e.printStackTrace();
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
        log.trace("Startup Event {}", ev);
    }

    @SuppressWarnings("unused")
    void onStop(@Observes ShutdownEvent ev) {
        log.info("Waiting for all tasks to finish.");
        // forceSleep();
        // TODO: Configurable timeout
        // Default fork join pool is used by Quarkus
        var pool = ForkJoinPool.commonPool();
        var threadCount = pool.getActiveThreadCount();
        log.debug("Waiting for {} common pool threads to finish.", threadCount);
        pool.awaitQuiescence(60, TimeUnit.MINUTES);
        // executor
        // log.debug("Waiting for executor ");
        // awaitTerminationAfterShutdown(executor);
        log.info("Cloud Janitor stopped.");
    }

    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}