package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import tasktree.Configuration;
import tasktree.aws.ResourceAction;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static tasktree.aws.ClientProducer.*;


public class FilterInstances implements Supplier<List<Instance>> {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);

    private final Configuration config;
    private final Region region;

    public FilterInstances(Configuration config, Region region) {
        this.config = config;
        this.region = region;
    }

    private boolean match(Instance instance) {
        boolean match = false;
        if (! instance.state().toString().equals("running")) return false;
        var prefix = config.getAwsCleanupPrefix();
        match = match || instance.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        return match;
    }

    private List<Instance> describeInstances(Region region) {
        var ec2 = newEC2Client(region);
        var describeInstances = DescribeInstancesRequest.builder().build();
        var reservations = ec2.describeInstancesPaginator(describeInstances).reservations().stream();
        var instances = reservations.flatMap(reservation -> reservation.instances().stream());
        var list = instances.filter(this::match).toList();
        log.info("Found {} instances in region [{}]", list.size(), region);
        return list;
    }


    @Override
    public List<Instance> get() {
        return describeInstances(region);
    }
}
