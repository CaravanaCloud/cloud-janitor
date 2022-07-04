package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSCleanup;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.Subnet;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

import static cloudjanitor.Input.AWS.TargetVpcId;

@Dependent
public class CleanupSubnets extends AWSCleanup {
    @Inject
    FilterSubnets filterSubnets;

    @Inject
    Instance<DeleteSubnet> deleteSubnet;

    @Override
    public List<Task> getDependencies() {
        return List.of(filterSubnets.withInput(TargetVpcId, inputString(TargetVpcId)));
    }

    @Override
    public void runSafe() {
        var subnets = outputList(Output.AWS.SubnetMatch, Subnet.class);
        subnets.forEach(this::deleteSubnet);
    }

    private void deleteSubnet(Subnet subnet) {
        var delSubnet = create(deleteSubnet).withSubnet(subnet);
        runTask(delSubnet);
    }
}
