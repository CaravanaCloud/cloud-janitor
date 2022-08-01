package cloudjanitor.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import javax.inject.Inject;
import java.util.*;

public class AWSClients {
    static final Logger log = LoggerFactory.getLogger(AWSClients.class);

    static final Map<Map<AWSIdentity, Region>, AWSClients> clients = new HashMap<>();

    private AWSClients(AWSConfiguration cfg,
                      AWSIdentity id,
                      Region region){
        this.cfg = cfg;
        this.id = id;
        this.region = region;
    }

    private final AWSConfiguration cfg;

    private final AWSIdentity id;
    private Region region;
    //TODO: cache by identity and region
    public static AWSClients of(AWSConfiguration config, AWSIdentity identity, Region region) {
        assert config != null;
        assert identity != null;
        assert region != null;
        var key = Map.of(identity, region);
        var value = clients.get(key);
        if (value == null ){
            log.debug("Creating AWS clients {} - {}", identity, region);
            value = new AWSClients(config, identity, region);
            clients.put(key, value);
        }else {
            log.trace("Found AWS clients {} - {}", identity, region);
        }
        return value;
    }

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
        return Region.of(cfg.defaultRegion());
    }
    // Clients //

    public StsClient sts() {
        var sts = StsClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return sts;
    }

    public IamClient iam() {
        var iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .credentialsProvider(getCredentialsProvider())
                .build();
        return iam;
    }

    public Ec2Client ec2(){
        return ec2(getRegion());
    }

    public Ec2Client ec2(Region region){
        var ec2 = Ec2Client.builder()
                .region(region)
                .credentialsProvider(getCredentialsProvider())
                .build();
        return ec2;
    }


    public CloudFormationClient cloudFormation(){
        return CloudFormationClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public S3TransferManager s3tx(){
        var tx = S3TransferManager
                .builder()
                .s3ClientConfiguration(c -> {
                    c.region(getRegion());
                    c.credentialsProvider(getCredentialsProvider());
                })
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
        var transcribe = TranscribeClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return transcribe;
    }

    public S3Client s3(Region region){
        var s3 = S3Client.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return s3;
    }

    public Route53Client route53() {
        return Route53Client.builder()
                .credentialsProvider(getCredentialsProvider())
                .region(Region.AWS_GLOBAL)
                .build();
    }

    public ElasticLoadBalancingV2Client elbv2() {
        return ElasticLoadBalancingV2Client.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public ElasticLoadBalancingClient elb() {
        return ElasticLoadBalancingClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public AthenaClient athena() {
        return AthenaClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public AWSConfiguration config(){
        return cfg;
    }

    public AwsCredentialsProvider getCredentialsProvider() {
        return id.toCredentialsProvider();
    }

}
