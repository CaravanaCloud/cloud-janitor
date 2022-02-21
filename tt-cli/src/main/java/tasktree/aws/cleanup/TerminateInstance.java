package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceState;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import tasktree.BaseProbe;
import tasktree.aws.ClientProducer;
import tasktree.spi.Sample;

import static tasktree.aws.ClientProducer.*;
import static tasktree.spi.Sample.*;

public class TerminateInstance extends BaseProbe {
    private final Instance instance;
    private final Region region;

    public TerminateInstance(Region region, Instance instance) {
        this.instance = instance;
        this.region = region;
    }

    @Override
    public Sample call() {
        var state = instance.state();
        if (state.toString().equals("running")) {
            log().info("Terminating instance {}", instance);
            var terminateInstance = TerminateInstancesRequest.builder()
                    .instanceIds(instance.instanceId())
                    .build();
            var ec2 = newEC2Client(region);
            ec2.terminateInstances(terminateInstance);
        }else {
            log().info("Not terminating instance {} {}",state ,instance);
        }
        return success();
    }
}
