package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import tasktree.BaseProbe;
import tasktree.Configuration;
import tasktree.spi.Sample;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static tasktree.aws.ClientProducer.newEC2Client;

public class FilterRegions implements Supplier<List<Region>> {
    private static Logger log = LoggerFactory.getLogger(FilterRegions.class);

    Configuration config;

    @Inject
    public FilterRegions(Configuration config) {
        this.config = config;
    }


    private static final List<Region> filterRegions(Configuration config) {
        var ec2 = newEC2Client();
        var regions = ec2.describeRegions().regions().stream()
                .map(software.amazon.awssdk.services.ec2.model.Region::regionName)
                .filter(config::filterRegion)
                .map(Region::of)
                .toList();
        log.info("Filtered regions {}", regions);
        return regions;
    }

    @Override
    public List<Region> get() {
        return filterRegions(config);
    }
}
