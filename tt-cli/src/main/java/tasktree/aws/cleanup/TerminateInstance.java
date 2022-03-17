package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import tasktree.Configuration;

public class TerminateInstance extends AWSDelete<Instance> {
    public TerminateInstance(Instance instance) {
        super(instance);
    }

    @Override
    public void cleanup(Instance resource) {
        var state = resource.state().nameAsString().toLowerCase();
        if (state.toString().equals("running")) {
            log().debug("Terminating instance {}", resource);
            var terminateInstance = TerminateInstancesRequest.builder()
                    .instanceIds(resource.instanceId())
                    .build();
            var ec2 = newEC2Client();
            ec2.terminateInstances(terminateInstance);
        }else {
            log().info("Not terminating instance {} {}",state ,resource);
        }
    }

    @Override
    protected String getResourceType() {
        return "EC2 Instance";
    }
}
