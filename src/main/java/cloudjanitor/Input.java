package cloudjanitor;

public interface Input {
    enum Local implements Input {
        fileExtension
    }

    enum AWS implements Input {
        targetVPCId,
        vpcCIDR,
        routeTable,
        targetLoadBalancerArn,
        targetLoadBalancerName,
        targetNatGatewayId,
        targetNetworkInterfaceId,
        targetNetworkInterface,
        resourceRecordSet,
        targetRouteTable,
        targetTargetGroup,
        targetVPCEndpoint,
        address,
        targetBucketName,
        targetRegion,
        identity,
        targetInstanceId,
        awsClients,
        targetAddress, securityGroupRule
    }
}
