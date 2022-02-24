package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import tasktree.Configuration;

public class DeleteLoadBalancerDescription extends AWSWrite {
    private final LoadBalancerDescription resource;

    public DeleteLoadBalancerDescription(Configuration config, LoadBalancerDescription resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().info("Deleting Classic ELB {}", resource.loadBalancerName());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerName(resource.loadBalancerName())
                .build();
        getELBClient().deleteLoadBalancer(request);
    }
}
