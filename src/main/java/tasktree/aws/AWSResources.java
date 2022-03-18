package tasktree.aws;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InternetGateway;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import tasktree.aws.cleanup.AWSAccount;

public enum AWSResources {

    Subnet;

    public static <T> String getDescription(T t) {
        var className = t.getClass().getSimpleName();
        return switch (className) {
            case "" -> ((Object)t).toString();
            case "LoadBalancer" -> ((LoadBalancer)t).loadBalancerName();
            case "TargetGroup" -> ((TargetGroup)t).targetGroupName();
            case "Address" -> ((Address)t).allocationId();
            case "NatGateway" -> ((NatGateway)t).natGatewayId();
            case "InternetGateway" -> ((InternetGateway)t).internetGatewayId();
            case "Subnet" -> ((Subnet)t).subnetId();
            case "Instance" -> ((Instance)t).instanceId();
            case "SecurityGroup" -> ((SecurityGroup)t).groupId();
            case "RouteTable" -> ((RouteTable)t).routeTableId();
            case "NetworkInterface" -> ((NetworkInterface)t).networkInterfaceId();
            case "VpcEndpoint" -> ((VpcEndpoint)t).vpcEndpointId();
            case "LoadBalancerDescription" -> ((LoadBalancerDescription)t).loadBalancerName();
            case "Vpc" -> ((Vpc)t).vpcId();
            case "AWSAccount" -> ((AWSAccount) t).accountId();
            case "Region" -> ((Region) t).id();
            default -> t.toString();
        };
    }
}
