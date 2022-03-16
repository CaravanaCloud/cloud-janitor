package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import tasktree.Configuration;

public class TerminateInstance extends AWSDelete {
    private final Instance instance;

    public TerminateInstance(Instance instance) {
        this.instance = instance;
    }

    public void runSafe() {
        var state = instance.state().nameAsString().toLowerCase();
        if (state.toString().equals("running")) {
            log().info("Terminating instance {}", instance);
            var terminateInstance = TerminateInstancesRequest.builder()
                    .instanceIds(instance.instanceId())
                    .build();
            var ec2 = newEC2Client();
            ec2.terminateInstances(terminateInstance);
        }else {
            log().info("Not terminating instance {} {}",state ,instance);
        }
    }

    @Override
    protected String getResourceType() {
        return "EC2 Instance";
    }
}
