package tasktree.aws.cleanup.ec2;

import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import tasktree.aws.AWSFilter;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterLoadBalancers extends AWSFilter<LoadBalancerDescription> {
    private String vpcId;

    public FilterLoadBalancers(String vpcId) {
        this.vpcId = vpcId;
    }

    private boolean match(LoadBalancerDescription resource) {
        var prefix = getAwsCleanupPrefix();
        var match = resource.vpcId().equals(vpcId);
        return match;
    }

    @Override
    protected List<LoadBalancerDescription> filterResources() {
        var elb = aws.getELBClient(getRegionOrDefault());
        var resources = elb.describeLoadBalancers().loadBalancerDescriptions();
        var matches = resources.stream().filter(this::match).toList();
        return matches;
    }

    @Override
    protected Stream<Task> mapSubtasks(LoadBalancerDescription resource) {
        return Stream.of(new DeleteLoadBalancerDescription(resource));
    }

    @Override
    protected String getResourceType() {
        return "Classic Load Balancer";
    }

}
