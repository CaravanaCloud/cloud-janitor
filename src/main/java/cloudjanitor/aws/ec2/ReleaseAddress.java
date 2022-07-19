package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class ReleaseAddress extends AWSWrite {

    @Override
    public void apply() {
        var resource = inputAs(Input.AWS.Address, Address.class);
        log().debug("Releasing address {}", resource.get().publicIp());
        var request = ReleaseAddressRequest.builder().allocationId(resource.get().allocationId()).build();
        aws().ec2().releaseAddress(request);
        success();
    }

}
