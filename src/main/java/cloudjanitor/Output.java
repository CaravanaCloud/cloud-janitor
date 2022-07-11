package cloudjanitor;

import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

public interface Output {
    enum Sample implements Output{
        Message, UpperMessage
        ;
    }

    enum AWS implements Output{
        Account,
        VPCMatch,
        SubnetMatch,
        ELBV2Match, 
        VPCId,
        InternetGatewayMatch, RouteTablesMatch, InstancesMatch, SecurityGroupRulesMatch, IpPermissionsMatch, SecurityGroupsMatch, LoadBalancerMatch, AddressMatch, LBDescriptionMatch, NatGatewaysMatch, NetworkINterfacesMatch, RegionMatches, RouteTableRulesMatch, TargetGroupsMatch, VPCEndpointsMatch;
    }
}
