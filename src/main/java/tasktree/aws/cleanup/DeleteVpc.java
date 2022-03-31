package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class DeleteVpc extends AWSDelete<Vpc> {
    public DeleteVpc(Vpc resource) {
        super(resource);
    }

    @Override
    public void cleanup(Vpc resource) {
        log().debug("Deleting VPC [{}]", resource.vpcId());
        var request = DeleteVpcRequest.builder()
                .vpcId(resource.vpcId())
                .build();
        newEC2Client().deleteVpc(request);
    }

    @Override
    protected String getResourceType() {
        return "VPC";
    }

    @Override
    public Stream<Task> mapSubtasks(Vpc resource) {
        var vpcId = resource.vpcId();
        return Stream.of(
                //TODO: Filter by VPC
                new FilterLoadBalancersV2(),
                new FilterTargetGroups(),
                new FilterNATGateways(),
                new FilterInstances(),
                //VPC Resources
                new FilterLoadBalancers(vpcId),
                new FilterVPCEndpoints(vpcId),
                new FilterSecurityGroupRules(vpcId),
                new FilterSecurityGroups(vpcId),
                new FilterNetworkInterfaces(vpcId),
                new FilterSubnets(vpcId),
                new FilterInternetGateways(vpcId),
                new FilterRouteTables(vpcId),
                //TODO: Filter by VPC
                new FilterAddresses()
        );
    }
}
