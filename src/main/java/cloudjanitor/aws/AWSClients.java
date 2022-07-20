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
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.*;

@Dependent
public class AWSClients {
    @Inject
    AWSConfiguration awsConfig;

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
        return Region.of(awsConfig.defaultRegion());
    }
    // Clients //

    public StsClient sts() {
        var sts = StsClient.builder().region(getRegion()).build();
        return sts;
    }

    public Ec2Client ec2(){
        return ec2(getRegion());
    }

    public Ec2Client ec2(Region region){
        var ec2 = Ec2Client.builder().region(region).build();
        return ec2;
    }


    public CloudFormationClient cloudFormation(){
        return CloudFormationClient.builder().region(getRegion()).build();
    }


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
                case "S3Client" -> s3(region);
                case "Route53Client" -> route53();
                case "Ec2Client" -> ec2(region);
                case "StsClient" -> sts();
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



    public S3TransferManager s3tx(){
        var tx = S3TransferManager
                .builder()
                .s3ClientConfiguration(c -> c.region(getRegion()))
                .build();
        return tx;
    }
    public S3Client s3(){
        return s3(getRegion());
    }

    public TranscribeClient transcribe(){
        return transcribe(getRegion());
    }

    private TranscribeClient transcribe(Region region) {
        var transcribe = TranscribeClient.builder().region(getRegion()).build();
        return transcribe;
    }

    public S3Client s3(Region region){
        var s3 = S3Client.builder().region(getRegion()).build();
        return s3;
    }

    public Route53Client route53() {
        return Route53Client.builder().region(Region.AWS_GLOBAL).build();
    }

    public ElasticLoadBalancingV2Client elbv2() {
        return ElasticLoadBalancingV2Client.builder()
                .region(getRegion())
                .build();
    }

    public ElasticLoadBalancingClient elb() {
        return ElasticLoadBalancingClient.builder()
                .region(getRegion())
                .build();
    }


    public AthenaClient athena() {
        return AthenaClient.builder().region(getRegion()).build();
    }


    public AWSConfiguration config(){
        return awsConfig;
    }

}
