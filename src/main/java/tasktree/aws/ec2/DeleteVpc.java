package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.aws.AWSDelete;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.stream.Stream;

@Dependent
public class DeleteVpc extends AWSDelete<String/*VpcId*/> {

    public DeleteVpc(){};

    public DeleteVpc(String resource/*VpcId*/) {
        super(resource);
    }

    @Override
    public void cleanup(String resource) {
        log().debug("Deleting VPC [{}]", resource);
        var request = DeleteVpcRequest.builder()
                .vpcId(resource)
                .build();
        newEC2Client().deleteVpc(request);
    }

    @Override
    protected String getResourceType() {
        return "VPC";
    }

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
}
