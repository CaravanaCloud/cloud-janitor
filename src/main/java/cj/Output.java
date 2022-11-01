package cj;

public interface Output {
    enum fs implements Output {
        paths
    }

    enum Local implements Output {
        FilesMatch

    }

    enum Sample implements Output{
        Message, UpperMessage
    }

    enum AWS implements Output{
        CallerIdentity,
        VPCMatch,
        SubnetMatch,
        ELBV2Match, 
        VPCId,
        InternetGatewayMatch, RouteTablesMatch, InstancesMatch, SecurityGroupRulesMatch, IpPermissionsMatch, SecurityGroupsMatch, AddressMatch, LBDescriptionMatch, NatGatewaysMatch, NetworkINterfacesMatch, RegionMatches, RouteTableRulesMatch, TargetGroupsMatch, VPCEndpointsMatch, S3Bucket, classicLoadBalancerMatch, Identities
    }
}
