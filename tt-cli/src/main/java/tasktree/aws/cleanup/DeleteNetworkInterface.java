package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import tasktree.Configuration;

public class DeleteNetworkInterface extends AWSDelete {
    private final NetworkInterface resource;

    public DeleteNetworkInterface(Configuration config, NetworkInterface resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().debug("Deleting ENI {}", resource.networkInterfaceId());
        var request = DeleteNetworkInterfaceRequest.builder().networkInterfaceId(resource.networkInterfaceId()).build();
        newEC2Client().deleteNetworkInterface(request);
    }

    @Override
    public String toString() {
        return super.toString("Network Interface",
                resource.networkInterfaceId());
    }
}
