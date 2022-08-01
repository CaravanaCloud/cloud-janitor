package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;

public class DeleteLoadBalancer extends AWSWrite {

    @Override
    public void apply() {
        var albArn = getInput(Input.AWS.TargetLoadBalancerArn, String.class);
        info("Deleting ELBV2 {}", albArn);
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(albArn)
                .build();
        aws().elbv2().deleteLoadBalancer(request);
        success();
    }
}
