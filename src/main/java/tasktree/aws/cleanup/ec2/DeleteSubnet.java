package tasktree.aws.cleanup.ec2;

import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import tasktree.aws.AWSDelete;

public class DeleteSubnet extends AWSDelete<Subnet> {
    public DeleteSubnet(Subnet net) {
        super(net);
    }

    @Override
    public void cleanup(Subnet resource) {
        log().debug("Deleting subnet " + resource.subnetId());
        DeleteSubnetRequest delSub = DeleteSubnetRequest.builder().subnetId(resource.subnetId()).build();
        newEC2Client().deleteSubnet(delSub);
    }

    @Override
    protected String getResourceType() {
        return "Subnet";
    }
}
