package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import tasktree.Configuration;

public class DeleteLoadBalancerDescription extends AWSDelete {
    private final LoadBalancerDescription resource;

    public DeleteLoadBalancerDescription(LoadBalancerDescription resource) {
        this.resource = resource;
    }

    @Override
    public void runSafe() {
        log().debug("Deleting Classic ELB {}", resource.loadBalancerName());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerName(resource.loadBalancerName())
                .build();
        aws.getELBClient(getRegion()).deleteLoadBalancer(request);
    }

    @Override
    public String getResourceDescription() {
        return
        resource.loadBalancerName();
    }

    @Override
    protected String getResourceType() {
        return "Classic Load Balancer";
    }
}
