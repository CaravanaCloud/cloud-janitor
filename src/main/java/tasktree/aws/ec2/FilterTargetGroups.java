package tasktree.aws.ec2;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import tasktree.aws.AWSFilter;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterTargetGroups extends AWSFilter<TargetGroup> {
    private boolean match(TargetGroup resource) {
        var prefix = getAwsCleanupPrefix();
        var match = resource.targetGroupName().startsWith(prefix);
        return match;
    }

    @Override
    protected List<TargetGroup> filterResources() {
        var elb = aws().getELBClientV2(getRegionOrDefault());
        var resources = elb.describeTargetGroups().targetGroups();
        var matches = resources.stream().filter(this::match).toList();
        return matches;
    }

    @Override
    public Stream<Task> mapSubtasks(TargetGroup resource) {
        return Stream.of(new DeleteTargetGroup(resource));
    }

    @Override
    protected String getResourceType() {
        return "Target Group";
    }

}
