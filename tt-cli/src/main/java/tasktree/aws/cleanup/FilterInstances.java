package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;


public class FilterInstances extends AWSFilter<Instance> {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);

    private boolean match(Instance instance) {
        var match = false;
        var state = instance.state().nameAsString().toLowerCase();
        var running = state.equals("running");
        if (!running) return false;
        var prefix = getConfig().getAwsCleanupPrefix();
        match = match || instance.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        return match;
    }

    private List<Instance> filterInstances() {
        var ec2 = newEC2Client();
        var describeInstances = DescribeInstancesRequest.builder().build();
        var reservations = ec2.describeInstancesPaginator(describeInstances).reservations().stream();
        var instances = reservations.flatMap(reservation -> reservation.instances().stream()).toList();
        var found = instances.size();
        var matches = instances.stream().filter(this::match).toList();
        var matched = matches.size();
        log.info("Matched {}/{} instances in [{}]", matched, found, getRegion());
        return matches;
    }


    @Override
    public void run() {
        var instances = filterInstances();
        addAllTasks(terminateInstances(instances));
    }
    
    private Stream<Task> terminateInstances(List<Instance> instances) {
        return instances.stream().map(this::terminateInstance);
    }


    private Task terminateInstance(Instance i) {
        return new TerminateInstance(getConfig(), i);
    }
}
