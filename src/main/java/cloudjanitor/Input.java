package cloudjanitor;

public interface Input {
    enum AWS implements Input{
        TargetVpcId,
        VpcCIDR,
        RouteTable, TargetLoadBalancerArn, TargetLoadBalancerName, TargetNatGatewayId, TargetNetworkInterfaceId, TargetNetworkInterface, ResourceRecordSet, TargetRouteTable, TargetTargetGroup, TargetVPCEndpoint, Address, TargetBucketName;
    }
}
