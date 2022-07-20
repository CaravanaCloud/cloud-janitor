package cloudjanitor;

public interface Output {
    enum Local implements Output {
        FilesMatch

    }

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
        InternetGatewayMatch, RouteTablesMatch, InstancesMatch, SecurityGroupRulesMatch, IpPermissionsMatch, SecurityGroupsMatch, LoadBalancerMatch, AddressMatch, LBDescriptionMatch, NatGatewaysMatch, NetworkINterfacesMatch, RegionMatches, RouteTableRulesMatch, TargetGroupsMatch, VPCEndpointsMatch, S3Bucket;
    }
}
