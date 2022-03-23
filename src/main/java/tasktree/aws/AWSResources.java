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

import java.util.HashMap;
import java.util.Map;

public enum AWSResources {;

    public static <T> Map<String, String> getProperties(T t){
        var className = t.getClass().getSimpleName();
        Map<String,String> properties = switch (className) {
            case "LoadBalancer" -> Map.of("name",
                    ((LoadBalancer)t).loadBalancerName());
            case "TargetGroup" -> Map.of("name",
                    ((TargetGroup)t).targetGroupName());
            case "Address" -> Map.of("id",
                    ((Address)t).allocationId());
            case "NatGateway" -> Map.of("id",
                    ((NatGateway)t).natGatewayId());
            case "InternetGateway" -> Map.of("id",
                    ((InternetGateway)t).internetGatewayId());
            case "Subnet" -> Map.of("id",
                    ((Subnet)t).subnetId());
            case "Instance" -> Map.of("id",
                    ((Instance)t).instanceId());
            case "SecurityGroup" -> Map.of("id",
                    ((SecurityGroup)t).groupId());
            case "RouteTable" -> Map.of("id",
                    ((RouteTable)t).routeTableId());
            case "NetworkInterface" -> Map.of("id",
                    ((NetworkInterface)t).networkInterfaceId());
            case "VpcEndpoint" -> Map.of("id",
                    ((VpcEndpoint)t).vpcEndpointId());
            case "LoadBalancerDescription" -> Map.of("name",
                    ((LoadBalancerDescription)t).loadBalancerName());
            case "Vpc" -> Map.of("id",
                    ((Vpc)t).vpcId());
            case "AWSAccount" -> Map.of("id",
                    ((AWSAccount) t).accountId());
            case "Region" -> Map.of("name",
                    ((Region) t).id());
            default -> Map.of();
        };
        return properties;
    }

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
