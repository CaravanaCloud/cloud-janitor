package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import tasktree.aws.AWSDelete;

import java.util.Optional;

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
            waitForTermination(resource);
        }else {
            log().trace("Not terminating instance {} {}",state ,resource);
        }
    }

    private void waitForTermination(Instance resource) {
        //TODO: Actually check
        try {
            log().debug("Waiting instance termination...");
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getResourceType() {
        return "EC2 Instance";
    }

    @Override
    public Optional<Long> getWaitAfterRun() {
        return Optional.of(30_000L);
    }
}
