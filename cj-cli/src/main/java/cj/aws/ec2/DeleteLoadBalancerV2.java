package cj.aws.ec2;

import cj.Input;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteLoadBalancerV2 extends AWSWrite {

    @Override
    public void apply() {
        var albArn = getInput(Input.aws.targetLoadBalancerArn, String.class);
        info("Deleting ELBV2 {}", albArn);
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerArn(albArn)
                .build();
        aws().elbv2().deleteLoadBalancer(request);
        success();
    }
}
