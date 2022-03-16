package tasktree.aws;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import tasktree.aws.cleanup.AWSAccount;

public class AWSResources {

    public static <T> String getDescription(T t) {
        var className = t.getClass().getSimpleName();
        return switch (className) {
            case "" -> ((Object)t).toString();

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
