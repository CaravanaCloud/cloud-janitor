package cj.aws.ec2;


import cj.Input;
import cj.aws.AWSTask;
import cj.spi.Task;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.Output.AWS.InstancesMatch;

@Dependent
public class TerminateInstancesTask extends AWSTask {
    @Inject
    FilterInstances filterInstances;

    @Inject
    Instance<TerminateInstanceTask> terminateInstance;

    @Override
    public Task getDependency() {
        return filterInstances;
    }

    @Override
    public void apply() {
        var instances = filterInstances.outputList(InstancesMatch, software.amazon.awssdk.services.ec2.model.Instance.class);
        instances.forEach(this::terminate);
    }

    public void terminate(software.amazon.awssdk.services.ec2.model.Instance instance){
        var terminate = terminateInstance.get()
                .withInput(Input.aws.targetInstanceId, instance.instanceId());
        submit(terminate);
    }
}
