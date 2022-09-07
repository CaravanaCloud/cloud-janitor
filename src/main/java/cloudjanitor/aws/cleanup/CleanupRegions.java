package cloudjanitor.aws.cleanup;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.aws.ec2.FilterRegions;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cloudjanitor.Input.AWS.targetRegion;

@Dependent
public class CleanupRegions extends AWSTask {
    @Inject
    FilterRegions filterRegions;

    @Inject
    Instance<CleanupRegion> cleanupRegionInstance;

    @Override
    public Task getDependency() {
        return filterRegions;
    }

    @Override
    public void apply() {
        var regions = filterRegions.outputList(Output.AWS.RegionMatches, Region.class);
        var tasks = regions.stream().map(this::mapTask);
        tasks.forEach(this::submit);
    }

    private Task mapTask(Region region) {
        var task = cleanupRegionInstance.get()
                .withInput(targetRegion, region);
        return task;
    }

    private boolean filterRegion(Region region) {
        var match = true;
        var regions = aws().config().regions();
        if (regions.isPresent()){
            match = regions.get().contains(region);
        }
        return match;
    }
}
