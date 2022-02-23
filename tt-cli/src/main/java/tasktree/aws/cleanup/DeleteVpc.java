package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

public class DeleteVpc extends AWSTask {
    private  Vpc resource;

    public DeleteVpc(Configuration config, Vpc resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        deleteVPC();
    }


    private void deleteVPC() {
        log().info("Deleting VPC {}", resource.vpcId());
        var request = DeleteVpcRequest.builder()
                .vpcId(resource.vpcId())
                .build();
        newEC2Client().deleteVpc(request);
    }
}
