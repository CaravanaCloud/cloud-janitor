package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import tasktree.Configuration;

public class DeleteLoadBalancer extends AWSDelete {
    private final LoadBalancer resource;

    public DeleteLoadBalancer(LoadBalancer resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        log().info("Deleting ELBV2 {}", resource.loadBalancerArn());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(resource.loadBalancerArn())
                .build();
        aws.getELBClientV2(getRegion()).deleteLoadBalancer(request);
    }

    @Override
    public String toString() {
        return super.toString("Application Load Balancer",
                resource.loadBalancerName());
    }
}
