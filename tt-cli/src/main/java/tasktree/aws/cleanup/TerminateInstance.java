package tasktree.aws.cleanup;

import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.BaseResult;

import static tasktree.spi.BaseResult.*;

public class TerminateInstance extends AWSTask {
    private final Instance instance;

    public TerminateInstance(Configuration config, Instance instance) {
        super(config);
        this.instance = instance;
    }

    public void run() {
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
    public String toString() {
        return super.toString() +
                " [instanceId=%s]".formatted(
                        instance.instanceId());
    }
}
