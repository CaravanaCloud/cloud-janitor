package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

public class DeleteLoadBalancer extends AWSTask {
    private final LoadBalancer resource;

    public DeleteLoadBalancer(Configuration config, LoadBalancer resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().info("Deleting Target group {}", resource.loadBalancerArn());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(resource.loadBalancerArn())
                .build();
        newELBClient().deleteLoadBalancer(request);
    }
}
