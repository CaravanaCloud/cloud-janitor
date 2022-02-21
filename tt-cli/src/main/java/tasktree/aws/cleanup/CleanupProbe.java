package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;
import tasktree.BaseProbe;
import tasktree.Configuration;
import tasktree.spi.Sample;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.*;

@ApplicationScoped
public class CleanupProbe extends BaseProbe {
    static final Logger log = LoggerFactory.getLogger(CleanupProbe.class);
    @Inject
    Configuration config;

    @Override
    public Sample call() {
        log().info("Cleaning up AWS Resources");
        //config.userWait();
        var executor = createExecutor();
        var regions = supplyAsync(new FilterRegions(config), executor);
        regions.thenApply( rs -> visitRegion(executor, rs));
        waitFor(executor);
        return Sample.success();
    }

    private Void visitRegion(ExecutorService executor, List<Region> rs) {
        rs.forEach(region -> {
            log.info("Visiting region {}", region);
            var instances = supplyAsync(new FilterInstances(config, region), executor);
            instances.thenAccept(is -> visitInstances(executor, is));
        });
        return null;
    }

    private void visitInstances(ExecutorService executor, List<Instance> is) {
        log.info(" -- visitInstances -- ");
        is.forEach(x -> log.info("{}", x));
    }

    private void waitFor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
