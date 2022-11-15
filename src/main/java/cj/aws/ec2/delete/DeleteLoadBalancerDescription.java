package cj.aws.ec2.delete;

import cj.Input;
import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteLoadBalancerDescription extends AWSWrite {

    @Override
    public void apply() {
        var elbName = getInputString(AWSInput.targetLoadBalancerName);
        debug("Deleting Classic ELB {}", elbName);
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerName(elbName)
                .build();
        aws().elbv1().deleteLoadBalancer(request);
        success();
    }
}
