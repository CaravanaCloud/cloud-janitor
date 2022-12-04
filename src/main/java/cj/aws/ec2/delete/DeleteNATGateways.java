package cj.aws.ec2.delete;

import cj.Output;
import cj.aws.AWSInput;
import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterNATGateways;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.NatGateway;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import static cj.aws.AWSOutput.*;

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
        var xs = filterNATs.outputList(NatGatewaysMatch, NatGateway.class);
        xs.forEach(this::deleteResource);
    }

    private void deleteResource(NatGateway nat) {
        var delTask = deleteNATInstance.get()
                .withInput(AWSInput.targetNatGatewayId, nat.natGatewayId());
        submit(delTask);
    }
}
