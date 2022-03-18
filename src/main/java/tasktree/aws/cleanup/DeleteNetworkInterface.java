package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import tasktree.Configuration;

public class DeleteNetworkInterface extends AWSDelete<NetworkInterface> {

    public DeleteNetworkInterface(NetworkInterface resource) {
        super(resource);
    }

    @Override
    public void cleanup(NetworkInterface resource) {
        log().debug("Deleting ENI {}", resource.networkInterfaceId());
        var request = DeleteNetworkInterfaceRequest.builder().networkInterfaceId(resource.networkInterfaceId()).build();
        newEC2Client().deleteNetworkInterface(request);
    }

    @Override
    protected String getResourceType() {
        return "Network Interface";
    }

}
