package cj.aws.cleanup;

import cj.Output;
import cj.aws.AWSTask;
import cj.aws.filter.FilterRegion;
import cj.aws.filter.FilterRegions;
import cj.spi.Task;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import static cj.aws.AWSOutput.*;

import static cj.aws.AWSInput.targetRegion;
@Dependent
public class CleanupRegions extends AWSTask {
    @Inject
    FilterRegions filterRegions;

    @Inject
    Instance<FilterRegion> cleanupRegionInstance;

    @Override
    public Task getDependency() {
        return filterRegions;
    }

    @Override
    public void apply() {
        var regions = filterRegions.outputList(RegionMatches, Region.class);
        var tasks = regions.stream().map(this::mapTask);
        tasks.forEach(this::submit);
    }

    private Task mapTask(Region region) {
        @SuppressWarnings("redundant")
        var task = cleanupRegionInstance.get()
                .withInput(targetRegion, region);
        return task;
    }
}
