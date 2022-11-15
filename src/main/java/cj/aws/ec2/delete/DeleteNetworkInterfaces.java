package cj.aws.ec2.delete;

import cj.Input;
import cj.Output;
import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterNetworkInterfaces;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Dependent
public class DeleteNetworkInterfaces extends AWSTask {
    @Inject
    FilterNetworkInterfaces filterENIs;

    @Inject
    Instance<DeleteNetworkInterface> deleteENIInstance;

    @Override
    public Task getDependency() {
        return delegate(filterENIs);
    }

    @Override
    public void apply() {
        var lbs = filterENIs.outputList(Output.aws.NetworkINterfacesMatch, NetworkInterface.class);
        lbs.stream().forEach(this::deleteNetworkInterface);
    }

    private void deleteNetworkInterface(NetworkInterface eni) {
        var delLbTask = deleteENIInstance
                .get()
                .withInput(Input.aws.targetNetworkInterfaceId, eni.networkInterfaceId());
        submit(delLbTask);
    }
}
