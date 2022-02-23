package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;

public class FilterRegions extends AWSTask {
    private static Logger log = LoggerFactory.getLogger(FilterRegions.class);

    private final List<Region> filterRegions(Configuration config) {
        try {
            var ec2 = newEC2Client();
            var regions = ec2.describeRegions().regions().stream()
                    .map(software.amazon.awssdk.services.ec2.model.Region::regionName)
                    .filter(config::filterRegion)
                    .map(Region::of)
                    .toList();
            log.info("Filtered regions {}", regions);
            return regions;
        }catch (Exception e) {
            log.info("Failed to filter regions", e);
        }
        return List.of();
    }

    @Override
    public void run() {
        var regions = filterRegions(getConfig());
        var tasks = regions.stream().map(this::toTask);
        addAllTasks(tasks);
    }

    private Task toTask(Region region) {
        return new CleanupRegion(getConfig(), region);
    }
}
