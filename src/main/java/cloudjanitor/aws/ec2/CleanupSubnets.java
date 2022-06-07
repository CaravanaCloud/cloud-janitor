package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSCleanup;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class CleanupSubnets extends AWSCleanup {
    String vpcId;

    @Inject
    FilterSubnets filterSubnets;

    @Inject
    Instance<DeleteSubnet> deleteSubnet;

    @Override
    public List<Task> getDependencies() {
        return List.of(filterSubnets.withVpcId(vpcId));
    }

    @Override
    public void runSafe() {
        var subnets = findAsList(Output.AWS.SubnetMatch, Subnet.class);
        subnets.forEach(this::deleteSubnet);
    }

    private void deleteSubnet(Subnet subnet) {
        var delSubnet = create(deleteSubnet).withSubnet(subnet);
        runTask(delSubnet);
    }

    public CleanupSubnets withVpcId(String vpcId) {
        this.vpcId = vpcId;
        return this;
    }
}
