package cj.aws.cleanup;

import cj.Input;
import cj.aws.AWSFilter;
import cj.aws.ec2.cleanup.CleanupVPCs;
import cj.spi.Task;
import software.amazon.awssdk.regions.Region;

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
        var region = getInput(Input.aws.targetRegion, Region.class);
        info("Region cleaned up "+region);
    }
}
