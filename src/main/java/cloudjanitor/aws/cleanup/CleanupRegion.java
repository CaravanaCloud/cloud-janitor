package cloudjanitor.aws.cleanup;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.aws.ec2.CleanupVPCs;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.regions.Region;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class CleanupRegion extends AWSFilter {
    @Inject
    CleanupVPCs cleanupVPCs;

    @Override
    public List<Task> getDependencies() {
        return List.of(cleanupVPCs);
    }

    @Override
    public void apply() {
        var region = getInput(Input.AWS.TargetRegion, Region.class);
        info("Region cleaned up "+region);
    }
}
