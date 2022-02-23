package tasktree.aws.cleanup;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

public class DeleteTargetGroup extends AWSTask {
    private final TargetGroup resource;

    public DeleteTargetGroup(Configuration config, TargetGroup resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().info("Deleting Target group {}", resource.targetGroupArn());
        var request = DeleteTargetGroupRequest.builder()
                .targetGroupArn(resource.targetGroupArn())
                .build();
        newELBClient().deleteTargetGroup(request);
    }
}
