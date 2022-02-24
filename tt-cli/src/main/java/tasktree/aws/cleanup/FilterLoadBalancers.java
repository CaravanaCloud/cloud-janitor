package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterLoadBalancers extends AWSFilter<LoadBalancerDescription> {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);
    private String vpcId;

    public FilterLoadBalancers(Configuration config, String vpcId) {
        super(config);
        this.vpcId = vpcId;
    }

    private boolean match(LoadBalancerDescription resource) {
        var prefix = getConfig().getAwsCleanupPrefix();
        var match = resource.vpcId().equals(vpcId);
        log.info("Found Load Balancer {} {}", mark(match), resource);
        return match;
    }

    private List<LoadBalancerDescription> filterLBs() {
        var elb = getELBClient();
        var resources = elb.describeLoadBalancers().loadBalancerDescriptions();
        var matches = resources.stream().filter(this::match).toList();
        log.info("Matched {} Classic ELBs in region [{}]", matches.size(), getRegion());
        return matches;
    }

    @Override
    public void run() {
        var resources = filterLBs();
        addAllTasks(deleteLBs(resources));
    }


    private Stream<Task> deleteLBs(List<LoadBalancerDescription> resources) {
        return resources.stream().map(this::deleteLoadBalancer);
    }


    private Task deleteLoadBalancer(LoadBalancerDescription resource) {
        return new DeleteLoadBalancerDescription(getConfig(), resource);
    }
}
