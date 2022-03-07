package tasktree.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.s3.S3Client;
import tasktree.BaseTask;
import tasktree.Configuration;
import tasktree.spi.Task;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class AWSTask
        extends BaseTask {

    static final Region DEFAULT_REGION = Region.US_EAST_1;
    static final Logger log = LoggerFactory.getLogger(AWSTask.class);

    @ConfigProperty(name = "tt.aws.region", defaultValue = "us-east-1")
    String defaultRegion;

    @ConfigProperty(name = "tt.aws.cleanup.prefix", defaultValue = "prefix-to-cleanup")
    String awsCleanupPrefix;

    Region region;


    public AWSTask(){}

    public AWSTask(Configuration config){
        super(config);
    }

    public AWSTask(Configuration config, Region region) {
        super(config);
        this.region = region;
    }

    protected S3Client newS3Client(){
        var s3 = S3Client.builder().region(getRegion()).build();
        return s3;
    }

    protected Ec2Client newEC2Client() {
        return newEC2Client(getRegion());
    }

    public void setRegion(Region region){
        this.region = region;
    }

    protected Route53Client newRoute53Client() {
        return Route53Client.builder().region(getRegion()).build();
    }

    protected ElasticLoadBalancingV2Client getELBClientV2() {
        return ElasticLoadBalancingV2Client.builder().region(getRegion()).build();
    }

    protected ElasticLoadBalancingClient getELBClient() {
        return ElasticLoadBalancingClient.builder()
                .region(getRegion())
                .build();
    }

    protected Ec2Client newEC2Client(Region region) {
        return Ec2Client.builder().region(region).build();
    }

    protected Logger log() {
        return log;
    }

    protected CloudTrailClient newCloudTrailClient() {
        var region = getRegion();
        var client = CloudTrailClient.builder().region(region).build();
        return client;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + region + ")";
    }

    public void addTask(Task task){
        propagateConfig(task);
        getConfig().getTasks().addTask(task);
    }

    private void propagateConfig(Task task) {
        task.setConfig(getConfig());
        if (task instanceof AWSTask awstask) {
            awstask.setRegion(getRegion());
            awstask.setAwsCleanupPrefix(getAwsCleanupPrefix());
        }
    }

    public void addAllTasks(List<Task> tasks){
        addAllTasks(tasks.stream());
    }

    public void addAllTasks(Stream<Task> tasks){
        tasks.forEach(this::addTask);
    }

    public void addAllTasks(Task... tasks){
        addAllTasks(Arrays.asList(tasks));
    }

    protected String mark(Boolean match){
        return match? "x" : "o";
    }

    public void setAwsCleanupPrefix(String prefix){
        this.awsCleanupPrefix = prefix;
    }

    public String getAwsCleanupPrefix() {
        if (awsCleanupPrefix == null || awsCleanupPrefix.isEmpty()){
            log.warn("No cleanup prefix configured.  This is probably a mistake.");
        }
        return awsCleanupPrefix;
    }

    public boolean isDryRun() {
        if (getConfig().isDryRun())
            return true;
        else {
            var unsafe = unsafeConfig();
            if (unsafe)
                log.warn("Enforcing dry run due to unsafe configuration.");
            return false;
       }
    }

    private boolean unsafeConfig() {
        boolean shortPrefix = awsCleanupPrefix == null || (awsCleanupPrefix.length() < MIN_PREFIX_LENGTH);
        if (shortPrefix) {
            log.warn("Unsafe configuration: naming prefix too short {}", awsCleanupPrefix);
            return true;
        }
        return false;
    }

    private static final int MIN_PREFIX_LENGTH = 4;

    public String getDefaultRegion() {
        return defaultRegion;
    }

    public boolean filterRegion(String regionName) {
        if (getDefaultRegion() == null || getDefaultRegion().isEmpty())
            return true;
        else
            return regionName.startsWith(getDefaultRegion());
    }

    protected Region getRegion() {
        if (region == null){
            var defaultRegion = getDefaultRegion();
            if (defaultRegion == null || defaultRegion.isEmpty()){
                log.warn("No region set, using default region [{}] [{}]", DEFAULT_REGION, getSimpleName());
                region = DEFAULT_REGION;
            }
            region = Region.of(defaultRegion);
        }
        return region;
    }

    @PostConstruct
    public void postConstruct(){
        log.trace("Initializing AWS Task {}", this);
        if (region == null){
            region = Region.of(getDefaultRegion());
        }
    }
}
