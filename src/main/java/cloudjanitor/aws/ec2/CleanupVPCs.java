package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.spi.Task;
import cloudjanitor.aws.AWSCleanup;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

import static cloudjanitor.Output.AWS.*;

@Dependent
public class CleanupVPCs extends AWSCleanup {

    @Inject
    FilterVPCs filterVPCs;

    @Inject
    Instance<DeleteVPC> deleteVPC;


    @Override
    public void apply() {
        var vpcs = outputList(VPCMatch, Vpc.class);
        vpcs.forEach(this::deleteVPC);
    }

    private void deleteVPC(Vpc vpc) {
        var delVpc = create(deleteVPC)
                .withInput(Input.AWS.TargetVpcId, vpc.vpcId());
        //TODO: Consider submit() runTask()
        submit(delVpc);
    }

    @Override
    public Task getDependency() {
        return filterVPCs;
    }

    public void setTargetVPC(String vpcId) {
        filterVPCs.setTargetVPC(vpcId);
    }

    /*
    @Override
    public Stream<Task> mapSubtasks(String vpcId) {
        return Stream.of(
                //TODO: Filter by VPC
                new FilterLoadBalancersV2(),
                new FilterTargetGroups(),
                new FilterNATGateways(),
                new FilterInstances(),
                //VPC Resources
                new FilterLoadBalancers(vpcId),
                new FilterVPCEndpoints(vpcId),
                new FilterNetworkInterfaces(vpcId),
                new FilterRouteTableRules(vpcId),
                new FilterRouteTables(vpcId),
                new FilterInternetGateways(vpcId),
                new FilterSecurityGroupRules(vpcId),
                new FilterSecurityGroups(vpcId),
                //TODO: Filter by VPC
                new FilterAddresses(),
                new FilterSubnets(vpcId),
                new FilterInternetGateways(vpcId)
        );
    }
    */
}
