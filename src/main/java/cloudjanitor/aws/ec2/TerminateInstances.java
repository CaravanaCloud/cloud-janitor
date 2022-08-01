package cloudjanitor.aws.ec2;


import cloudjanitor.Output;
import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

import static cloudjanitor.Output.AWS.InstancesMatch;

@Dependent
public class TerminateInstances extends AWSWrite {
    @Inject
    FilterInstances filterInstances;

    @Override
    public Task getDependency() {
        return filterInstances;
    }

    @Override
    public void apply() {
        var instances = filterInstances.outputList(InstancesMatch, Instance.class);
        instances.forEach(this::terminate);
    }

    public void terminate(Instance instance){
        debug("Terminating instance {} ", instance);
        var terminateInstance = TerminateInstancesRequest.builder()
                .instanceIds(instance.instanceId())
                .build();
        var ec2 = aws().ec2();
        ec2.terminateInstances(terminateInstance);
        success();
    }
}
