package tasktree.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import tasktree.BaseTask;
import tasktree.Configuration;
import tasktree.spi.Task;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Stream;

public abstract class AWSTask<T>
        extends BaseTask {

    static final Region DEFAULT_REGION = Region.US_EAST_1;
    static final Logger log = LoggerFactory.getLogger(AWSTask.class);
    private static final int MIN_PREFIX_LENGTH = 4;
    protected List<T> resources = new ArrayList<>();
    protected List<Task> subtasks = null;
    protected Region region;
    protected AWSClients aws = AWSClients.getInstance();
    @ConfigProperty(name = "tt.aws.regions", defaultValue = "us-east-1")
    String targetRegions;
    Set<String> targetRegionsSet;
    @ConfigProperty(name = "tt.aws.cleanup.prefix", defaultValue = "prefix-to-cleanup")
    String awsCleanupPrefix;
    String resourceDescription = "";

    public AWSTask() {
    }

    public AWSTask(Configuration config) {
        super(config);
    }

    public AWSTask(Region newRegion) {
        setRegion(newRegion);
    }

    public AWSTask(Configuration config, Region region) {
        super(config);
        this.region = region;
    }

    protected S3Client newS3Client() {
        return aws.newS3Client(getRegion());
    }

    protected Ec2Client newEC2Client() {
        return aws.newEC2Client(getRegion());
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
        return asString(getName(),
                getResourceType(),
                getResourceDescription());
    }

    public void addTask(Task task) {
        propagateConfig(task);
        //getConfig().getTasks().addTask(task);
        getSubtasks().add(task);
    }

    public Task propagateConfig(Task task) {
        task.setConfig(getConfig());
        if (task instanceof AWSTask awstask) {
            var _region = getRegion();
            awstask.setTargetRegions(targetRegions);
            if (awstask.getRegion() == null) {
                awstask.setRegion(_region);
            }
            awstask.setAwsCleanupPrefix(getAwsCleanupPrefix());
        }
        return task;
    }

    private void setTargetRegions(String targetRegions) {
        this.targetRegions = targetRegions;
    }

    public void addAllTasks(List<Task> tasks) {
        addAllTasks(tasks.stream());
    }

    public void addAllTasks(Stream<Task> tasks) {
        tasks.forEach(this::addTask);
    }

    public void addAllTasks(Task... tasks) {
        addAllTasks(Arrays.asList(tasks));
    }

    protected String mark(Boolean match) {
        return match ? "x" : "o";
    }

    public String getAwsCleanupPrefix() {
        if (awsCleanupPrefix == null || awsCleanupPrefix.isEmpty()) {
            log.warn("No cleanup prefix configured.  This is probably a mistake.");
        }
        return awsCleanupPrefix;
    }

    public void setAwsCleanupPrefix(String prefix) {
        this.awsCleanupPrefix = prefix;
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

    public boolean filterRegion(String regionName) {
        if (targetRegions == null || targetRegions.isEmpty()) {
            log.warn("Target regions not set [tt.aws.regions], searching all.");
            return true;
        } else {
            var set = getTargetRegionsSet();
            var filter = set.contains(regionName);
            return filter;
        }
    }

    private Set<String> getTargetRegionsSet() {
        if (targetRegionsSet == null) {
            var split = targetRegions.split(",");
            targetRegionsSet = new HashSet<String>(Arrays.asList(split));
        }
        return targetRegionsSet;
    }

    protected Region getRegion() {
        if (region == null) {
            region = getDefaultRegion();
        }
        return region;
    }

    private Region getDefaultRegion() {
        var regionName = targetRegions.split(",")[0];
        if (! regionName.isEmpty()){
            log.debug("Using region [{}] as default", region);
        }
        var defaultRegion = Region.of(regionName);
        return defaultRegion;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    @PostConstruct
    public void postConstruct() {
        log.trace("Initializing AWS Task {}", this);
    }

    protected String getResourceType() {
        return "AWS Resource";
    }


    protected <T extends SdkClient> T getClient(Class<T> clientClass) {
        return aws.getClient(getRegion(), clientClass);
    }

    @Override
    public final List<Task> getSubtasks() {
        if (subtasks == null) {
            subtasks = resources
                    .stream()
                    .flatMap(this::mapSubtasks)
                    .map(this::propagateConfig)
                    .toList();
        }
        return subtasks;
    }

    protected <R> Stream<Task> mapSubtasks(T t) {
        return Stream.of();
    }

    public final String getResourceDescription() {
        var description = String.join(",", resources
                .stream()
                .map(AWSResources::getDescription)
                .toList());
        return description;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }
}