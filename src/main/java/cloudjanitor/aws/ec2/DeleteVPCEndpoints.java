package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Dependent
public class DeleteVPCEndpoints extends AWSTask {
    @Inject
    FilterVPCEndpoints filterVPCEs;

    @Inject
    Instance<DeleteVPCEndpoint> deleteVPCEInstance;

    @Override
    public Task getDependency() {
        return delegate(filterVPCEs);
    }

    @Override
    public void apply() {
        var xs = filterVPCEs.outputList(Output.AWS.VPCEndpointsMatch, VpcEndpoint.class);
        xs.forEach(this::deleteResource);
    }

    private void deleteResource(VpcEndpoint vpce) {
        var delTask = deleteVPCEInstance.get()
                .withInput(Input.AWS.targetVPCEndpoint, vpce);
        submit(delTask);
    }
}
