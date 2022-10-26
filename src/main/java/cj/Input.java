package cj;

public interface Input {
    enum Local implements Input {
        message, fileExtension
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
