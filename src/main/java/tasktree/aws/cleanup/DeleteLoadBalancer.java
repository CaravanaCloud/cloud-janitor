package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import tasktree.Configuration;

public class DeleteLoadBalancer extends AWSDelete<LoadBalancer> {

    public DeleteLoadBalancer(LoadBalancer resource) {
        super(resource);
    }

    @Override
    public void cleanup(LoadBalancer resource) {
        log().info("Deleting ELBV2 {}", resource.loadBalancerArn());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(resource.loadBalancerArn())
                .build();
        aws.getELBClientV2(getRegion()).deleteLoadBalancer(request);
    }

    @Override
    protected String getResourceType() {
        return "Application Load Balancer";
    }
}
