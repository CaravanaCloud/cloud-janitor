package cloudjanitor.aws.cleanup;

import cloudjanitor.aws.AWSFilter;
import cloudjanitor.aws.ec2.CleanupVPCs;
import cloudjanitor.spi.Task;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class CleanupRegion extends AWSFilter {
    @Inject
    CleanupVPCs deleteVPCs;

    @Override
    public List<Task> getDependencies() {
        return List.of(deleteVPCs);
    }

    @Override
    public void runSafe() {
        log().info("Region cleaned up "+aws().getRegion());
    }
}
