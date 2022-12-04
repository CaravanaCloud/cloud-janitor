package cj.aws.ec2.delete;

import cj.Output;
import cj.aws.AWSInput;
import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterVPCEndpoints;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import static cj.aws.AWSOutput.*;

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
        var xs = filterVPCEs.outputList(VPCEndpointsMatch, VpcEndpoint.class);
        xs.forEach(this::deleteResource);
    }

    private void deleteResource(VpcEndpoint vpce) {
        var delTask = deleteVPCEInstance.get()
                .withInput(AWSInput.targetVPCEndpoint, vpce);
        submit(delTask);
    }
}
