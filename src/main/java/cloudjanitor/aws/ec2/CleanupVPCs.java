package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.Ec2Client;
import cloudjanitor.aws.AWSCleanup;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class CleanupVPCs extends AWSCleanup {

    @Inject
    FilterVPCs filterVPCs;

    @Inject
    Instance<DeleteVPC> deleteVPC;


    @Override
    public void runSafe() {
        var vpcs = (List<Vpc>) findAsList("aws.vpc.matches");
        vpcs.forEach(this::deleteVPC);
    }

    private void deleteVPC(Vpc vpc) {
        var delVpc = create(deleteVPC)
                .withVPC(vpc);
        //TODO: Consider submit() runTask()
        tasks.runTask(delVpc);
    }

    @Override
    public List<Task> getDependencies() {
        return List.of(filterVPCs);
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
