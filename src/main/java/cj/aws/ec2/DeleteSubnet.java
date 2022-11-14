package cj.aws.ec2;

import cj.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteSubnet extends AWSWrite {
    String subnetId;

    public DeleteSubnet withSubnet(Subnet subnet) {
        this.subnetId = subnet.subnetId();
        return this;
    }

    public void apply() {
        DeleteSubnetRequest delSub = DeleteSubnetRequest.builder()
                .subnetId(subnetId)
                .build();
        aws().ec2().deleteSubnet(delSub);
        debug("Deleted subnet " + subnetId);
    }


}
