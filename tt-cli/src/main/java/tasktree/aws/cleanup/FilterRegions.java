package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class FilterRegions extends AWSFilter<List<Region>> {

    @Inject
    Instance<FilterRegion> filterRegionInstance;

    @Inject
    Logger log;

    List<Region> filterRegions() {
        try {
            var ec2 = newEC2Client();
            var regions = ec2.describeRegions()
                    .regions()
                    .stream()
                    .map(software.amazon.awssdk.services.ec2.model.Region::regionName)
                    .filter(config::filterRegion)
                    .map(Region::of)
                    .toList();
            log.debug("Matched regions [{}]", regions);
            return regions;
        }catch (Exception e) {
            log.info("Failed to filter regions", e);
        }
        return List.of();
    }

    @Override
    public void run() {
        var regions = filterRegions();
        var tasks = regions.stream().map(this::newFilterRegion);
        addAllTasks(tasks);
    }

    Task newFilterRegion(Region region) {
        var filterRegion = filterRegionInstance.get();
        filterRegion.setRegion(region);
        return filterRegion;
    }
}
