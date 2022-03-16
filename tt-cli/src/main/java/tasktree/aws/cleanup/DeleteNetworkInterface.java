package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import tasktree.Configuration;

public class DeleteNetworkInterface extends AWSDelete {
    private final NetworkInterface resource;

    public DeleteNetworkInterface(NetworkInterface resource) {
        this.resource = resource;
    }

    @Override
    public void runSafe() {
        log().debug("Deleting ENI {}", resource.networkInterfaceId());
        var request = DeleteNetworkInterfaceRequest.builder().networkInterfaceId(resource.networkInterfaceId()).build();
        newEC2Client().deleteNetworkInterface(request);
    }

    @Override
    protected String getResourceType() {
        return "Network Interface";
    }

    @Override
    public String getResourceDescription() {
        return super.getResourceDescription();
    }

}
