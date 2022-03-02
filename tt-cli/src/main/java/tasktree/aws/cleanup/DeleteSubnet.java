package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import tasktree.Configuration;

public class DeleteSubnet extends AWSDelete {
    private final Subnet net;

    public DeleteSubnet(Configuration config, Subnet net) {
        super(config);
        this.net = net;
    }

    @Override
    public void run() {
        deleteSubnet();
    }



    private void deleteSubnet() {
        log().debug("Deleting subnet " + net.subnetId());
        DeleteSubnetRequest delSub = DeleteSubnetRequest.builder().subnetId(net.subnetId()).build();
        newEC2Client().deleteSubnet(delSub);
    }

    @Override
    public String toString() {
        return super.toString("Subnet", net.subnetId());
    }
}
