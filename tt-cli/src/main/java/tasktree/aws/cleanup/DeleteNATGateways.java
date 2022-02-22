package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteNatGatewayRequest;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

import java.util.List;

public class DeleteNATGateways extends AWSTask {
    Ec2Client ec2 = newEC2Client();
    private final List<NatGateway> ns;

    public DeleteNATGateways(List<NatGateway> ns) {
        this.ns = ns;
    }

    @Override
    public void run() {
        terminateNATGateways(ns);;
    }

    private void terminateNATGateways(List<NatGateway> ns) {
        if (getConfig().isDryRun()) return;
        log().debug(" -- deleting [{}] nat gateways -- ", ns.size());
        ns.forEach(this::deleteNatGateway);
    }

    private void deleteNatGateway(NatGateway nat) {
        log().info("Deleting {}", nat);
        var deleteNat = DeleteNatGatewayRequest.builder().natGatewayId(nat.natGatewayId()).build();
        ec2.deleteNatGateway(deleteNat);
    }
}
