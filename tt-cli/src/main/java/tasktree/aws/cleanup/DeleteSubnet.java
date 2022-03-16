package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import tasktree.Configuration;

public class DeleteSubnet extends AWSDelete {
    private final Subnet net;

    public DeleteSubnet(Subnet net) {
        this.net = net;
    }

    @Override
    public void runSafe() {
        deleteSubnet();
    }



    private void deleteSubnet() {
        log().debug("Deleting subnet " + net.subnetId());
        DeleteSubnetRequest delSub = DeleteSubnetRequest.builder().subnetId(net.subnetId()).build();
        newEC2Client().deleteSubnet(delSub);
    }

    @Override
    public String getResourceDescription() {
        return  net.subnetId();
    }

    @Override
    protected String getResourceType() {
        return "Subnet";
    }
}
