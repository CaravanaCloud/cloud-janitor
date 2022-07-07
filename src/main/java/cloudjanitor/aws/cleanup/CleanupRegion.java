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
    public Task getDependency() {
        return deleteVPCs;
    }

    @Override
    public void apply() {
        log().info("Region cleaned up "+aws().getRegion());
    }
}
