package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import tasktree.Configuration;

public class ReleaseAddress extends AWSDelete {
    private final Address addr;

    public ReleaseAddress(Configuration config, Address addr) {
        super(config);
        this.addr = addr;
    }

    @Override
    public void run() {
        log().debug("Releasing address {}", addr.publicIp());
        var request = ReleaseAddressRequest.builder().allocationId(addr.allocationId()).build();
        newEC2Client().releaseAddress(request);
    }

    @Override
    public String toString() {
        return super.toString("Address", addr.publicIp(), addr.allocationId());
    }
}
