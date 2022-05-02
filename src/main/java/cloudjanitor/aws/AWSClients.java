package cloudjanitor.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.*;

@Dependent
public class AWSClients {
    @ConfigProperty(name = "cj.aws.region", defaultValue = "us-east-1")
    String awsDefaultRegion;

    private Region region;

    public Region getRegion() {
        if (region == null){
            region = getDefaultRegion();
        }
        return region;
    }

    public void setRegion(Region region){
        this.region = region;
    }

    protected Region getDefaultRegion(){
        return Region.of(awsDefaultRegion);
    }
    // Clients //

    public StsClient getSTSClient() {
        var sts = StsClient.builder().region(getRegion()).build();
        return sts;
    }

    public Ec2Client getEC2Client(){
        var ec2 = Ec2Client.builder().region(getRegion()).build();
        return ec2;
    }

    public CloudFormationClient getCloudFormationClient(){
        return CloudFormationClient.builder().region(getRegion()).build();
    }

    ///////////////////////////////////////////////////

    Map<Region, Map<Class<? extends SdkClient>, SdkClient>> clients = new HashMap<>();

    @ConfigProperty(name = "cj.aws.regions", defaultValue = "us-east-1")
    String targetRegions;

    @Inject
    Logger log;

    Set<String> targetRegionsSet;
    List<Region> targetRegionsList;


    @SuppressWarnings("unchecked")
    public <T extends SdkClient> T getClient(Region region, Class<T> clientClass) {
        var regionClients = clients.getOrDefault(region, new HashMap<>());
        var client = regionClients.get(clientClass);
        if (client == null) {
            client = switch (clientClass.getSimpleName()) {
                case "S3Client" -> newS3Client(region);
                case "Route53Client" -> newRoute53Client(region);
                case "Ec2Client" -> newEC2Client(region);
                case "StsClient" -> getSTSClient();
                default -> throw new IllegalArgumentException("Unknown client class: " + clientClass.getSimpleName());
            };
        }
        return (T) client;
    }

    public List<Region> getTargetRegionsList() {
        if (targetRegionsList == null) {
            var split = targetRegions.split(",");
            targetRegionsList = Arrays.asList(split)
                    .stream()
                    .map(Region::of)
                    .toList();
        }
        return targetRegionsList;
    }


    public Set<String> getTargetRegionsSet() {
        if (targetRegionsSet == null) {
            var split = targetRegions.split(",");
            targetRegionsSet = new HashSet<String>(Arrays.asList(split));
        }
        if (targetRegionsSet.isEmpty()){
            targetRegionsSet.add(getDefaultRegion().toString());
            log.warn("Target regions is empty. Using using default. {}", targetRegionsSet);
        }
        return targetRegionsSet;
    }



    public S3Client newS3Client(Region region){
        var s3 = S3Client.builder().region(region).build();
        return s3;
    }

    public Route53Client newRoute53Client(Region region) {
        return Route53Client.builder().region(region).build();
    }

    public ElasticLoadBalancingV2Client getELBClientV2() {
        return ElasticLoadBalancingV2Client.builder()
                .region(getRegion())
                .build();
    }

    public ElasticLoadBalancingClient getELBClient(Region region) {
        return ElasticLoadBalancingClient.builder()
                .region(region)
                .build();
    }

    public Ec2Client newEC2Client(Region region) {
        return Ec2Client.builder().region(region).build();
    }

    public AthenaClient newAthenaClient(Region region) {
        return AthenaClient.builder().region(region).build();
    }




    public String getTargetRegions() {
        return targetRegions;
    }


}
