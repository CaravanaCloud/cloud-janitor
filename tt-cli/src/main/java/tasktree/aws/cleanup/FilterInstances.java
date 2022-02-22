package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;
import tasktree.spi.Tasks;

import java.util.List;


public class FilterInstances extends AWSTask {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);

    private boolean match(Instance instance) {
        boolean match = false;
        if (! instance.state().toString().equals("running")) return false;
        var prefix = getConfig().getAwsCleanupPrefix();
        match = match || instance.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        return match;
    }

    private List<Instance> filterInstances(Region region) {
        var ec2 = newEC2Client(region);
        var describeInstances = DescribeInstancesRequest.builder().build();
        var reservations = ec2.describeInstancesPaginator(describeInstances).reservations().stream();
        var instances = reservations.flatMap(reservation -> reservation.instances().stream());
        var matches = instances.filter(this::match).toList();
        log.info("Matched {} instances in region [{}]", matches.size(), region);
        return matches;
    }


    @Override
    public void run() {
        var tasks = filterInstances(getRegion())
                .stream()
                .map(this::toTask);
        pushAll(tasks);
    }


    private Task toTask(Instance i) {
        return new TerminateInstance(getConfig(), i);
    }
}
