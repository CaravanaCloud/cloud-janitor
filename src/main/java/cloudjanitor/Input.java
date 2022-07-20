package cloudjanitor;

public interface Input {
    enum  Local implements Input{
        FileExtension

    }

    enum AWS implements Input{
        TargetVpcId,
        VpcCIDR,
        RouteTable, TargetLoadBalancerArn, TargetLoadBalancerName, TargetNatGatewayId, TargetNetworkInterfaceId, TargetNetworkInterface, ResourceRecordSet, TargetRouteTable, TargetTargetGroup, TargetVPCEndpoint, Address, TargetBucketName;
    }
}
