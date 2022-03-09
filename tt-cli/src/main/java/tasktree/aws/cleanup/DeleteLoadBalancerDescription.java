package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import tasktree.Configuration;

public class DeleteLoadBalancerDescription extends AWSDelete {
    private final LoadBalancerDescription resource;

    public DeleteLoadBalancerDescription(Configuration config, LoadBalancerDescription resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().debug("Deleting Classic ELB {}", resource.loadBalancerName());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerName(resource.loadBalancerName())
                .build();
        aws.getELBClient(getRegion()).deleteLoadBalancer(request);
    }

    @Override
    public String toString() {
        return super.toString("Classic Load Balancer",
                resource.loadBalancerName());
    }
}
