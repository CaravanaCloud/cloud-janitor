package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import tasktree.aws.AWSFilter;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;


public class FilterInstances extends AWSFilter<Instance> {

    private boolean match(Instance instance) {
        var match = false;
        var state = instance.state().nameAsString().toLowerCase();
        var running = state.equals("running");
        if (!running) return false;
        var prefix = getAwsCleanupPrefix();
        match = match || instance.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        return match;
    }

    @Override
    protected List<Instance> filterResources() {
        var ec2 = newEC2Client();
        var describeInstances = DescribeInstancesRequest.builder().build();
        var reservations = ec2.describeInstancesPaginator(describeInstances).reservations().stream();
        var instances = reservations.flatMap(reservation -> reservation.instances().stream()).toList();
        var matches = instances.stream().filter(this::match).toList();
        return matches;
    }

    @Override
    protected Stream<Task> mapSubtasks(Instance instance) {
        return Stream.of(new TerminateInstance(instance));
    }

    @Override
    protected String getResourceType() {
        return "EC2 Instance";
    }
}
