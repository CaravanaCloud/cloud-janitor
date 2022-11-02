package cj.aws.ec2;

import cj.Input;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteLoadBalancerV1 extends AWSWrite {
    @Override
    public void apply() {
        var albName = getInput(Input.aws.targetLoadBalancerName, String.class);
        info("Deleting ELBV1 {}", albName);
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerName(albName)
                .build();
        aws().elbv1().deleteLoadBalancer(request);
        success();
    }
}
