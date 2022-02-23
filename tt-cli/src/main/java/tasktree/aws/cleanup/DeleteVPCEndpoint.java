package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteRouteTableRequest;
import software.amazon.awssdk.services.ec2.model.DeleteVpcEndpointsRequest;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

public class DeleteVPCEndpoint extends AWSTask {
    private final VpcEndpoint resource;

    public DeleteVPCEndpoint(Configuration config, VpcEndpoint resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        log().info("Deleting vpc endpoint {}", resource.vpcEndpointId());
        var request = DeleteVpcEndpointsRequest.builder()
                .vpcEndpointIds(resource.vpcEndpointId())
                .build();
        newEC2Client().deleteVpcEndpoints(request);
    }
}
