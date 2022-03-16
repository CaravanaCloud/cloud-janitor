package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteNatGatewayRequest;
import software.amazon.awssdk.services.ec2.model.NatGateway;

import java.util.List;

public class DeleteNATGateway extends AWSDelete {
    NatGateway resource;

    public DeleteNATGateway(NatGateway resource) {
        this.resource = resource;
    }

    private void deleteNatGateway(NatGateway nat) {
        log().debug("Deleting {}", nat);
        var deleteNat = DeleteNatGatewayRequest.builder().natGatewayId(nat.natGatewayId()).build();
        var ec2 = newEC2Client();
        ec2.deleteNatGateway(deleteNat);
    }

    @Override
    public void runSafe() {
        deleteNatGateway(resource);
    }
}
