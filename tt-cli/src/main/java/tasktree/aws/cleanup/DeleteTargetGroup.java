package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import tasktree.Configuration;

public class DeleteTargetGroup extends AWSDelete {
    private final TargetGroup resource;

    public DeleteTargetGroup(TargetGroup resource) {
         this.resource = resource;
    }

    @Override
    public void runSafe() {
        log().debug("Deleting Target group {}", resource.targetGroupArn());
        var request = DeleteTargetGroupRequest.builder()
                .targetGroupArn(resource.targetGroupArn())
                .build();
        aws.getELBClientV2(getRegion()).deleteTargetGroup(request);
    }

    @Override
    public String getResourceDescription() {
        return resource.targetGroupName();
    }

    @Override
    protected String getResourceType() {
        return "Target Group";
    }


}
