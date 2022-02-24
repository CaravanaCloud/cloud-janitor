package tasktree.aws;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class AWSTask
        extends BaseTask {
    static final Region DEFAULT_REGION = Region.US_EAST_1;
    static final Logger log = LoggerFactory.getLogger(AWSTask.class);

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

    protected Region getRegion() {
        var result = region;
        if (result == null && config != null) {
            result = config.getRegion();
        };
        if (result == null) {
            result = DEFAULT_REGION;
        }
        return result;
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
        return getSimpleName() + " (" + getRegion() + ")";
    }

    public void addTask(Task task){
        task.setConfig(config);
        if (task instanceof AWSTask)
            ((AWSTask)task).setRegion(region);
        getConfig().getTasks().addTask(task);
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
}
