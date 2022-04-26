package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import cloudjanitor.aws.AWSCleanup;

public class ReleaseAddress extends AWSCleanup<Address> {
    public ReleaseAddress(Address addr) {
        super(addr);
    }

    @Override
    public void cleanup(Address resource) {
        log().debug("Releasing address {}", resource.publicIp());
        var request = ReleaseAddressRequest.builder().allocationId(resource.allocationId()).build();
        aws().newEC2Client(getRegion()).releaseAddress(request);
    }

    @Override
    protected String getResourceType() {
        return "Address";
    }


}