package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DeleteNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

public class DeleteNetworkInterface extends AWSTask {
    private final NetworkInterface resource;

    public DeleteNetworkInterface(Configuration config, NetworkInterface resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().info("Deleting ENI {}", resource.networkInterfaceId());
        var request = DeleteNetworkInterfaceRequest.builder().networkInterfaceId(resource.networkInterfaceId()).build();
        newEC2Client().deleteNetworkInterface(request);
    }
}
