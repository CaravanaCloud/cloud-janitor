package cloudjanitor.aws;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.cleanup.CleanupRegion;
import cloudjanitor.aws.ec2.FilterRegions;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cloudjanitor.Input.AWS.TargetRegion;

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
        var matches = regions.stream().filter(this::filterRegion).toList();
        log().info("Matched {}/{} regions. {}", matches.size(), regions.size(), matches);
        var tasks = matches.stream().map(this::mapTask);
        tasks.forEach(this::submit);
    }

    private Task mapTask(Region region) {
        var task = cleanupRegionInstance.get()
                .withInput(TargetRegion, region);
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
