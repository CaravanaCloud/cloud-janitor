package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import tasktree.Configuration;

public class DeleteLoadBalancer extends AWSWrite {
    private final LoadBalancer resource;

    public DeleteLoadBalancer(Configuration config, LoadBalancer resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().info("Deleting ELBV2 {}", resource.loadBalancerArn());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(resource.loadBalancerArn())
                .build();
        getELBClientV2().deleteLoadBalancer(request);
    }
}
