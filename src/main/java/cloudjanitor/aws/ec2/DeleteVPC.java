package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.lang.annotation.Target;
import java.util.List;

import static cloudjanitor.Input.AWS.TargetVpcId;

@Dependent
public class DeleteVPC extends AWSWrite {

    @Inject
    CleanupSubnets cleanupSubnets;

    @Inject
    CleanupRouteTables cleanRouteTables;


    @Override
    public void runSafe() {
        var vpcId = inputString(TargetVpcId);
        var request = DeleteVpcRequest.builder()
                .vpcId(vpcId)
                .build();
        aws().getEC2Client().deleteVpc(request);
        log().debug("Deleted VPC {}/{}", getRegion(), vpcId);
    }


    @Override
    public List<Task> getDependencies() {
        return List.of(
                cleanupSubnets.input(TargetVpcId, input(TargetVpcId)),
                cleanRouteTables.input(TargetVpcId, input(TargetVpcId))
        );
    }
}
