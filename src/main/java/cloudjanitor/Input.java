package cloudjanitor;

public interface Input {
    enum AWS implements Input{
        TargetVpcId,
        VpcCIDR,
        RouteTable;
    }
}
