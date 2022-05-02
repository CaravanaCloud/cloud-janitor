package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import cloudjanitor.aws.AWSCleanup;

public class DeleteSubnet extends AWSCleanup {
    /*
    public DeleteSubnet(Subnet net) {
        super(net);
    }

    @Override
    public void cleanup(Subnet resource) {
        log().debug("Deleting subnet " + resource.subnetId());
        DeleteSubnetRequest delSub = DeleteSubnetRequest.builder().subnetId(resource.subnetId()).build();
        aws().newEC2Client(getRegion()).deleteSubnet(delSub);
    }

    @Override
    protected String getResourceType() {
        return "Subnet";
    }

     */
}
