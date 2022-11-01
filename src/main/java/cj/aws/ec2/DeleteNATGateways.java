package cj.aws.ec2;

import cj.Input;
import cj.Output;
import cj.aws.AWSTask;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.NatGateway;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
@Dependent
public class DeleteNATGateways extends AWSTask {
    @Inject
    FilterNATGateways filterNATs;

    @Inject
    Instance<DeleteNATGateway> deleteNATInstance;

    @Override
    public Task getDependency() {
        return delegate(filterNATs);
    }

    @Override
    public void apply() {
        var xs = filterNATs.outputList(Output.AWS.NatGatewaysMatch, NatGateway.class);
        xs.forEach(this::deleteResource);
    }

    private void deleteResource(NatGateway nat) {
        var delTask = deleteNATInstance.get()
                .withInput(Input.aws.targetNatGatewayId, nat.natGatewayId());
        submit(delTask);
    }
}
