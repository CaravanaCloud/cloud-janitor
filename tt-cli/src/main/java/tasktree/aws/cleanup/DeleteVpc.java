package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.Configuration;

public class DeleteVpc extends AWSDelete {
    Vpc resource;

    public DeleteVpc(Vpc resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        deleteVPC();
    }


    private void deleteVPC() {
        log().trace("Deleting VPC [{}]", resource.vpcId());
        var request = DeleteVpcRequest.builder()
                .vpcId(resource.vpcId())
                .build();
        newEC2Client().deleteVpc(request);
    }

    @Override
    public String toString() {
        return super.toString("VPC", resource.vpcId());
    }
}
