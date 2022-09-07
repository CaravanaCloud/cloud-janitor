package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.Address;
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
                .withInput(Input.AWS.targetNatGatewayId, nat.natGatewayId());
        submit(delTask);
    }
}
