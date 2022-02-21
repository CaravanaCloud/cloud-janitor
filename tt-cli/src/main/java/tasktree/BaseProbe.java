package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tasktree.aws.cleanup.TerminateInstance;
import tasktree.spi.Probe;
import tasktree.spi.Probes;
import tasktree.spi.Sample;

import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseProbe<T> implements Probe<T> {
    private static final Logger log = LoggerFactory.getLogger(Probe.class);


    static final ExecutorService executor = createExecutor();

    protected ExecutorService getExecutor() {
        return executor;
    }

    public static final ExecutorService createExecutor() {
        return Executors.newWorkStealingPool();
    }

    @Override
    public Sample call() {
        log().info("🐣");
        return Sample.empty();
    }

    protected Logger log() {
        return log;
    }
}
