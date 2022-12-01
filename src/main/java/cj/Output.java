package cj;

public interface Output {
    enum shell implements Output {
        exitCode, stderr, stdout

    }

    enum fs implements Output {
        paths
    }

    enum local implements Output {
        FilesMatch

    }

    enum sample implements Output{
        Message, UpperMessage
    }

    enum aws implements Output{
        CallerIdentity,
        VPCMatch,
        SubnetMatch,
        ELBV2Match, 
        VPCId,
        InternetGatewayMatch, RouteTablesMatch, InstancesMatch, SecurityGroupRulesMatch, IpPermissionsMatch, SecurityGroupsMatch, AddressMatch, LBDescriptionMatch, NatGatewaysMatch, NetworkINterfacesMatch, RegionMatches, RouteTableRulesMatch, TargetGroupsMatch, VPCEndpointsMatch, S3Bucket, classicLoadBalancerMatch, Identities
    }
}
