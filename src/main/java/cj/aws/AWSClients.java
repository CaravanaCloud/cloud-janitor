package cj.aws;

import cj.shell.ShellTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
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
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.util.HashMap;
import java.util.Map;

public class AWSClients {
    static final Logger log = LoggerFactory.getLogger(AWSClients.class);

    static final Map<Map<AWSIdentity, Region>, AWSClients> clients = new HashMap<>();

    private AWSClients(AWSConfiguration cfg,
                      AWSIdentity id){
        this.cfg = cfg;
        this.id = id;
    }

    private final AWSConfiguration cfg;

    private final AWSIdentity id;
    //TODO: consider caching AWSClients by identity / region
    public static AWSClients of(AWSConfiguration config, AWSIdentity identity) {
        var region = getRegion(config);
        var key = Map.of(identity, region);
        var value = clients.get(key);
        if (value == null ){
            log.debug("Creating new AWSClients for {} - {}", identity, region);
            value = new AWSClients(config, identity);
            clients.put(key, value);
        }else {
            log.trace("Using cached AWSClients for {} - {}", identity, region);
        }
        return value;
    }

    public static Region getRegion(AWSConfiguration config) {
        var region = getDefaultRegion(config);
        if (region == null){
            region = getCLIRegion();
        }else {
            region = Region.US_EAST_1;
        }
        return region;
    }

    private static Region getCLIRegion() {
        var cliRegion = ShellTask.execute("aws", "configure", "get", "region");
        return cliRegion.map(Region::of).orElse(null);
    }


    protected static Region getDefaultRegion(AWSConfiguration config){
        //TODO: Use "aws configure get region" to get configured region
        var defaultRegion = config.defaultRegion();
        if (defaultRegion != null){
            return Region.of(defaultRegion);
        }
        return null;
    }
    // Clients //

    public StsClient sts() {
        @SuppressWarnings("redundant")
        var sts = StsClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return sts;
    }

    public IamClient iam() {
        @SuppressWarnings("redundant")
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
        @SuppressWarnings("redundant")
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

    public S3TransferManager s3tm(){
        @SuppressWarnings("redundant")
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
        @SuppressWarnings("redundant")
        var transcribe = TranscribeClient.builder()
                .region(region)
                .credentialsProvider(getCredentialsProvider())
                .build();
        return transcribe;
    }

    public TranslateClient translate(){
        return translate(getRegion());
    }

    private TranslateClient translate(Region region) {
        @SuppressWarnings("redundant")
        var translate = TranslateClient.builder()
                .region(region)
                .credentialsProvider(getCredentialsProvider())
                .build();
        return translate;
    }

    public S3Client s3(Region region){
        @SuppressWarnings("redundant")
        var s3 = S3Client.builder()
                .region(region)
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

    public ElasticLoadBalancingClient elbv1() {
        return ElasticLoadBalancingClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public ElasticLoadBalancingV2Client elbv2() {
        return ElasticLoadBalancingV2Client.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @SuppressWarnings("unused")
    public AthenaClient athena() {
        @SuppressWarnings("redundant")
        var athena = AthenaClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return athena;
    }

    public AWSConfiguration config(){
        return cfg;
    }

    public AwsCredentialsProvider getCredentialsProvider() {
        return id.toCredentialsProvider();
    }


    public Region getRegion() {
        return getRegion(config());
    }
}
