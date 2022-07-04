package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import cloudjanitor.aws.AWSCleanup;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteSubnet extends AWSCleanup {
    String subnetId;

    public DeleteSubnet withSubnet(Subnet subnet) {
        this.subnetId = subnet.subnetId();
        return this;
    }

    public void runSafe() {
        DeleteSubnetRequest delSub = DeleteSubnetRequest.builder()
                .subnetId(subnetId)
                .build();
        aws().ec2().deleteSubnet(delSub);
        log().debug("Deleted subnet " + subnetId);
    }


}
