package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteLoadBalancerDescription extends AWSWrite {

    @Override
    public void apply() {
        var elbName = getInputString(Input.AWS.TargetLoadBalancerName);
        debug("Deleting Classic ELB {}", elbName);
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerName(elbName)
                .build();
        aws().elb().deleteLoadBalancer(request);
        success();
    }
}
