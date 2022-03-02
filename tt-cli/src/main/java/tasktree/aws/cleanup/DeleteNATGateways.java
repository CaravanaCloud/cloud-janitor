package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteNatGatewayRequest;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import tasktree.Configuration;

import java.util.List;

public class DeleteNATGateways extends AWSDelete {
    Ec2Client ec2 = newEC2Client();
    private final List<NatGateway> ns;

    public DeleteNATGateways(Configuration config, List<NatGateway> ns) {
        super(config);
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
        log().debug("Deleting {}", nat);
        var deleteNat = DeleteNatGatewayRequest.builder().natGatewayId(nat.natGatewayId()).build();
        ec2.deleteNatGateway(deleteNat);
    }

    @Override
    public String toString() {
        return super.toString("NAT Gateways",
                ns.stream()
                        .map(n->n.natGatewayId().toString())
                        .toString()
                        .join(", "));
    }
}
