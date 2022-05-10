package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class DeleteVPC extends AWSWrite {
    String vpcId;

    @Inject
    CleanupSubnets cleanupSubnets;

    @Override
    public void runSafe() {
        var request = DeleteVpcRequest.builder()
                .vpcId(vpcId)
                .build();
        aws().getEC2Client().deleteVpc(request);
        log().debug("Deleted VPC {}/{}", getRegion(), vpcId);
    }

    public DeleteVPC withVPC(Vpc vpc) {
        this.vpcId = vpc.vpcId();
        return this;
    }

    @Override
    public List<Task> getDependencies() {
        return List.of(cleanupSubnets.withVpcId(vpcId));
    }
}
