package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteVpcEndpointsRequest;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import tasktree.Configuration;

public class DeleteVPCEndpoint extends AWSDelete {
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

    @Override
    public String toString() {
        return super.toString("VPC Endpoint", resource.vpcEndpointId());
    }
}
