package cj.aws.ec2;

import cj.Input;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteTargetGroup extends AWSWrite {

    @Override
    public void apply() {
        var resource = getInput(Input.AWS.targetTargetGroup, TargetGroup.class);
        debug("Deleting Target group {}", resource.targetGroupArn());
        var request = DeleteTargetGroupRequest.builder()
                .targetGroupArn(resource.targetGroupArn())
                .build();
        aws().elbv2().deleteTargetGroup(request);
    }

}
