package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

public class ReleaseAddress extends AWSWrite {
    private final Address addr;

    public ReleaseAddress(Configuration config, Address addr) {
        super(config);
        this.addr = addr;
    }

    @Override
    public void run() {
        log().info("Releasing address {}", addr.publicIp());
        var request = ReleaseAddressRequest.builder().allocationId(addr.allocationId()).build();
        newEC2Client().releaseAddress(request);
    }
}
