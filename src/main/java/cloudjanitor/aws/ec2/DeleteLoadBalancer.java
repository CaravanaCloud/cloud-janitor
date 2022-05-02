package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import cloudjanitor.aws.AWSCleanup;

public class DeleteLoadBalancer extends AWSCleanup {
    /*

    public DeleteLoadBalancer(LoadBalancer resource) {
        super(resource);
    }

    @Override
    public void cleanup(LoadBalancer resource) {
        log().info("Deleting ELBV2 {}", resource.loadBalancerArn());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(resource.loadBalancerArn())
                .build();
        aws().getELBClientV2().deleteLoadBalancer(request);
    }

     */
}
