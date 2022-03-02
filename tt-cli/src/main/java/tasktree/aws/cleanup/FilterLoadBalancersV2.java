package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterLoadBalancersV2 extends AWSFilter<LoadBalancer> {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);

    private boolean match(LoadBalancer resource) {
        var prefix = getConfig().getAwsCleanupPrefix();
        var match = resource.loadBalancerName().startsWith(prefix);
        log.trace("Found Load Balancer V2 {} {}", mark(match), resource);
        return match;
    }

    private List<LoadBalancer> filterLBs() {
        var elb = getELBClientV2();
        var resources = elb.describeLoadBalancers().loadBalancers();
        var matches = resources.stream().filter(this::match).toList();
        log.info("Matched [{}] Load Balancers V2 in region [{}]", matches.size(), getRegion());
        return matches;
    }


    @Override
    public void run() {
        var resources = filterLBs();
        addAllTasks(deleteLBs(resources));
    }


    private Stream<Task> deleteLBs(List<LoadBalancer> subnets) {
        return subnets.stream().map(this::deleteLoadBalancer);
    }


    private Task deleteLoadBalancer(LoadBalancer resource) {
        return new DeleteLoadBalancer(resource);
    }
}
