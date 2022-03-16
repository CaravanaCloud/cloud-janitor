package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import tasktree.Configuration;

public class ReleaseAddress extends AWSDelete {
    private final Address addr;

    public ReleaseAddress(Address addr) {
        this.addr = addr;
    }

    @Override
    public void runSafe() {
        log().debug("Releasing address {}", addr.publicIp());
        var request = ReleaseAddressRequest.builder().allocationId(addr.allocationId()).build();
        newEC2Client().releaseAddress(request);
    }

    @Override
    protected String getResourceType() {
        return "Address";
    }

    @Override
    public String getResourceDescription() {
        return "%s %s".formatted(addr.publicIp(), addr.allocationId());
    }

}
