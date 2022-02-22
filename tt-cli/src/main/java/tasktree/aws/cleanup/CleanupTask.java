package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.DeleteNatGatewayRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Tasks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.*;

@ApplicationScoped
public class CleanupTask extends AWSTask {
    static final Logger log = LoggerFactory.getLogger(CleanupTask.class);

    @Inject
    Configuration config;

    @Override
    public void run() {
        log.info("Cleaning up AWS Resources");
        setConfig(config);
        setRegion(config.getRegion());
        push(new FilterRegions());
    }


}
