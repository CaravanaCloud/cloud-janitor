package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import tasktree.aws.AWSDelete;

public class ReleaseAddress extends AWSDelete<Address> {
    public ReleaseAddress(Address addr) {
        super(addr);
    }

    @Override
    public void cleanup(Address resource) {
        log().debug("Releasing address {}", resource.publicIp());
        var request = ReleaseAddressRequest.builder().allocationId(resource.allocationId()).build();
        newEC2Client().releaseAddress(request);
    }

    @Override
    protected String getResourceType() {
        return "Address";
    }


}
