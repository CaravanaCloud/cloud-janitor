package tasktree.aws;

import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;

import java.util.HashMap;
import java.util.Map;

public class AWSClients {
    Map<Region, Map<Class<? extends SdkClient>, SdkClient>> clients = new HashMap<>();

    public <T extends SdkClient> T getClient(Region region, Class<T> clientClass) {
        var regionClients = clients.getOrDefault(region, new HashMap<>());
        var client = regionClients.get(clientClass);
        if (client == null) {
            client = switch (clientClass.getSimpleName()) {
                case "S3Client" -> newS3Client(region);
                case "Route53Client" -> newRoute53Client(region);
                case "Ec2Client" -> newEC2Client(region);
                case "StsClient" -> newSTSClient(region);
                default -> throw new IllegalArgumentException("Unknown client class: " + clientClass.getSimpleName());
            };
        }
        return (T) client;
    }

    private StsClient newSTSClient(Region region) {
        var sts = StsClient.builder().region(region).build();
        return sts;
    }

    public S3Client newS3Client(Region region){
        var s3 = S3Client.builder().region(region).build();
        return s3;
    }

    public Route53Client newRoute53Client(Region region) {
        return Route53Client.builder().region(region).build();
    }

    public ElasticLoadBalancingV2Client getELBClientV2(Region region) {
        return ElasticLoadBalancingV2Client.builder().region(region).build();
    }

    public ElasticLoadBalancingClient getELBClient(Region region) {
        return ElasticLoadBalancingClient.builder()
                .region(region)
                .build();
    }

    public Ec2Client newEC2Client(Region region) {
        return Ec2Client.builder().region(region).build();
    }


}
