package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import cloudjanitor.aws.AWSCleanup;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class DeleteVPCs extends AWSCleanup {

    @Inject
    FilterVPCs filterVPCs;

    @PostConstruct
    public void resolveDependencies(){
        dependsOn(filterVPCs);
    }

    @Override
    public void runSafe() {
        var vpcs = (List<Vpc>) findAsList("aws.vpc.matches");
        var ec2 = aws().getEC2Client();
        vpcs.forEach(vpc -> deleteVPC(ec2, vpc));
        log().debug("VPCs deleted");
    }

    private void deleteVPC(Ec2Client ec2, Vpc vpc) {
        var request = DeleteVpcRequest.builder()
                .vpcId(vpc.vpcId())
                .build();
        ec2.deleteVpc(request);
        log().debug("Deleted VPC [{}]", vpc);
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
