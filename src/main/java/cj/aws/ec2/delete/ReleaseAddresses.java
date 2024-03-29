package cj.aws.ec2.delete;

import cj.aws.AWSInput;
import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterAddresses;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.Address;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.aws.AWSOutput.AddressMatch;

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
        var eips = filterEIPs.outputList(AddressMatch, Address.class);
        eips.stream().forEach(this::deleteAddress);
    }

    private void deleteAddress(Address eip) {
        var delTask = deleteEIPInstance.get()
                .withInput(AWSInput.address, eip);
        submit(delTask);
    }
}
