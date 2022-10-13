package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.Address;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Dependent
public class ReleaseAddresses extends AWSTask {
    @Inject
    FilterAddresses filterEIPs;

    @Inject
    Instance<ReleaseAddress> deleteEIPInstance;

    @Override
    public Task getDependency() {
        return delegate(filterEIPs);
    }

    @Override
    public void apply() {
        var eips = filterEIPs.outputList(Output.AWS.AddressMatch, Address.class);
        eips.stream().forEach(this::deleteAddress);
    }

    private void deleteAddress(Address eip) {
        var delTask = deleteEIPInstance.get()
                .withInput(Input.AWS.address, eip);
        submit(delTask);
    }
}