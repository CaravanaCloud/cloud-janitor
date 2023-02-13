package cj.aws;

import org.slf4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClient;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AWSClients {

    @Inject
    Logger log;

    @Inject
    AWSConfiguration cfg;

    @Inject
    AWSClientsManager clientsManager;

    AwsCredentialsProvider credentialsProvider;

    Region region;

    public StsClient sts() {
        @SuppressWarnings("redundant")
        var sts = StsClient.builder()
                .region(region())
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
        return ec2(region());
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
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public S3TransferManager s3tm(){
        @SuppressWarnings("redundant")
        var tx = S3TransferManager
                .builder()
                .s3Client(s3async())
                .build();
        return tx;
    }

    private S3AsyncClient s3async() {
        return S3AsyncClient.builder()
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public S3Client s3(){
        return s3(region());
    }

    public TranscribeClient transcribe(){
        return transcribe(region());
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
        return translate(region());
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
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public ElasticLoadBalancingV2Client elbv2() {
        return ElasticLoadBalancingV2Client.builder()
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @SuppressWarnings("unused")
    public AthenaClient athena() {
        @SuppressWarnings("redundant")
        var athena = AthenaClient.builder()
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return athena;
    }

    public SsmClient ssm() {
        var ssm = SsmClient.builder()
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return ssm;
    }

    public AWSConfiguration config(){
        return cfg;
    }

    public AwsCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }


    public CloudTrailClient cloudtrail() {
        var _region = region();
        return CloudTrailClient.builder()
                .region(_region)
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public Region region() {
        return region;
    }

    public void setCredentialsProvider(AwsCredentialsProvider creds) {
        this.credentialsProvider = creds;
    }

    public void setRegion(Region region) {
        this.region = region;
    }


    public ResourceGroupsTaggingApiClient tagging() {
        var tagging = ResourceGroupsTaggingApiClient.builder()
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return tagging;
    }

    public CloudWatchClient cloudwatch() {
        var cw = CloudWatchClient.builder()
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return cw;
    }

    public CloudWatchLogsClient cloudwatchlogs() {
        var cw = CloudWatchLogsClient.builder()
                .region(region())
                .credentialsProvider(getCredentialsProvider())
                .build();
        return cw;
    }
}
