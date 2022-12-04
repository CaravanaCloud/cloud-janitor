package cj.aws.ec2.delete;

import cj.aws.AWSWrite;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

import static cj.aws.AWSInput.targetVPCId;
@Dependent
public class DeleteVPC extends AWSWrite {

    @Inject
    DeleteSubnets cleanupSubnets;

    @Inject
    DeleteRouteTables cleanRouteTables;

    @Inject
    DeleteInternetGateways  deleteInternetGateways;

    @Inject
    TerminateInstancesTask terminateInstances;

    @Inject
    DeleteSecurityGroupRules deleteSecurityGroupRules;

    @Inject
    DeleteSecurityGroups deleteSecurityGroups;

    @Inject
    DeleteLoadBalancersV2 deleteLoadBalancersV2;

    @Inject
    DeleteLoadBalancersV1 deleteLoadBalancersV1;

    @Inject
    DeleteNetworkInterfaces deleteNetworkInterfaces;

    @Inject
    ReleaseAddresses deleteAddresses;

    @Inject
    DeleteNATGateways deleteNATGateways;

    @Inject
    DeleteVPCEndpoints deleteVPCEndpoints;

    @Override
    public void apply() {
        var vpcId = getInputString(targetVPCId);
        var request = DeleteVpcRequest.builder()
                .vpcId(vpcId)
                .build();
        aws().ec2().deleteVpc(request);
        debug("Deleted VPC {}/{}", region(), vpcId);
    }


    @Override
    public List<Task> getDependencies() {
        return delegateAll(
                terminateInstances,
                deleteLoadBalancersV2,
                deleteLoadBalancersV1,
                deleteNATGateways,
                deleteVPCEndpoints,
                deleteAddresses,
                deleteNetworkInterfaces,
                deleteSecurityGroupRules,
                deleteSecurityGroups,
                cleanupSubnets,
                cleanRouteTables,
                deleteInternetGateways
        );
    }


}
