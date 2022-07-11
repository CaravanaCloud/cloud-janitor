package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

import static cloudjanitor.Input.AWS.TargetVpcId;

@Dependent
public class DeleteVPC extends AWSWrite {

    @Inject
    DeleteSubnets cleanupSubnets;

    @Inject
    DeleteRouteTables cleanRouteTables;

    @Inject
    DeleteInternetGateways deleteInternetGateways;

    @Inject
    TerminateInstances terminateInstances;

    @Inject
    DeleteSecurityGroupRules deleteSecurityGroupRules;

    @Inject
    DeleteSecurityGroups deleteSecurityGroups;

    @Inject
    DeleteLoadBalancers deleteLoadBalancers;

    @Override
    public void apply() {
        var vpcId = getInputString(TargetVpcId);
        var request = DeleteVpcRequest.builder()
                .vpcId(vpcId)
                .build();
        aws().ec2().deleteVpc(request);
        log().debug("Deleted VPC {}/{}", getRegion(), vpcId);
    }


    @Override
    public List<Task> getDependencies() {
        return delegateAll(
                terminateInstances,
                deleteLoadBalancers,
                deleteSecurityGroupRules,
                deleteSecurityGroups,
                cleanupSubnets,
                cleanRouteTables,
                deleteInternetGateways
        );
    }


}
