package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterLoadBalancersV2 extends AWSFilter<LoadBalancer> {

    private boolean match(LoadBalancer resource) {
        var prefix = getAwsCleanupPrefix();
        var match = resource.loadBalancerName().startsWith(prefix);
        return match;
    }

    @Override
    protected List<LoadBalancer> filterResources() {
        var elb = aws.getELBClientV2(getRegionOrDefault());
        var resources = elb.describeLoadBalancers().loadBalancers();
        var matches = resources.stream().filter(this::match).toList();
        return matches;
    }


    @Override
    protected Stream<Task> mapSubtasks(LoadBalancer lb) {
        return Stream.of(deleteLoadBalancer(lb));
    }


    private Task deleteLoadBalancer(LoadBalancer resource) {
        return new DeleteLoadBalancer(resource);
    }

    @Override
    protected String getResourceType() {
        return "Load Balancer V2";
    }
}
