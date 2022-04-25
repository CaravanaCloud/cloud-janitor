package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import cloudjanitor.aws.AWSCleanup;

public class DeleteTargetGroup extends AWSCleanup<TargetGroup> {
    public DeleteTargetGroup(TargetGroup resource) {
         super(resource);
    }

    @Override
    public void cleanup(TargetGroup resource) {
        log().debug("Deleting Target group {}", resource.targetGroupArn());
        var request = DeleteTargetGroupRequest.builder()
                .targetGroupArn(resource.targetGroupArn())
                .build();
        aws().getELBClientV2(getRegionOrDefault()).deleteTargetGroup(request);
    }

    @Override
    protected String getResourceType() {
        return "Target Group";
    }


}
