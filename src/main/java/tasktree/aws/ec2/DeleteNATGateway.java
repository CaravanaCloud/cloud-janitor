package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.DeleteNatGatewayRequest;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import tasktree.aws.AWSDelete;

public class DeleteNATGateway extends AWSDelete<NatGateway> {
    public DeleteNATGateway(NatGateway resource) {
        super(resource);
    }

    @Override
    protected void cleanup(NatGateway resource) {
        log().debug("Deleting {}", resource);
        var deleteNat = DeleteNatGatewayRequest.builder().natGatewayId(resource.natGatewayId()).build();
        var ec2 = newEC2Client();
        ec2.deleteNatGateway(deleteNat);
    }

    @Override
    protected String getResourceType() {
        return "NAT Gateway";
    }
}
