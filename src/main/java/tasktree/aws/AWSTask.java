package tasktree.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.s3.S3Client;
import tasktree.BaseTask;
import tasktree.Configuration;
import tasktree.Result;
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
    protected static final AWSClients aws = AWSClients.getInstance();
    @ConfigProperty(name = "tt.aws.regions", defaultValue = "us-east-1")
    String targetRegions;
    Set<String> targetRegionsSet;

    @ConfigProperty(name = "tt.aws.cleanup.prefix")
    Optional<String> awsCleanupPrefix;

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
        return aws.newS3Client(getRegionOrDefault());
    }

    protected Ec2Client newEC2Client() {
        return aws.newEC2Client(getRegionOrDefault());
    }

    protected Logger log() {
        return LoggerFactory.getLogger(getName());
    }

    protected CloudTrailClient newCloudTrailClient() {
        var region = getRegionOrDefault();
        var client = CloudTrailClient.builder().region(region).build();
        return client;
    }

    @Override
    public String toString() {
        return asString(getName(),
                getRegionOrDefault().toString(),
                getResultType(),
                getDescription());
    }

    public void addTask(Task task) {
        propagateConfig(task);
        //getConfig().getTasks().addTask(task);
        getSubtasks().add(task);
    }

    public Task propagateConfig(Task task) {
        task.setConfig(getConfig());
        if (task instanceof AWSTask awstask) {
            var taskName = awstask.getName();
            awstask.setTargetRegions(targetRegions);
            if (awstask.getRegion() == null) {
                awstask.setRegion(getRegion());
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
        return awsCleanupPrefix.orElse("");
    }

    public void setAwsCleanupPrefix(String prefix) {
        this.awsCleanupPrefix = Optional.of(prefix);
    }

    public boolean isDryRun() {
        return getConfig().isDryRun();
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

    protected Region getRegion(){
        return region;
    }

    protected Region getRegionOrDefault() {
        if (region == null) {
            return getDefaultRegion();
        }
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    private Region getDefaultRegion() {
        if (region != null) return region;
        var regionName = targetRegions.split(",")[0];
        if (regionName == null || regionName.isEmpty()) {
            regionName = DEFAULT_REGION.id();
            log.debug("Using region [{}] as default", region);
        }
        var defaultRegion = Region.of(regionName);
        return defaultRegion;
    }

    @PostConstruct
    public void postConstruct() {
        log.trace("Initializing AWS Task {}", this);
    }

    protected String getResourceType() {
        return "AWS Resource";
    }


    protected <T extends SdkClient> T getClient(Class<T> clientClass) {
        return aws.getClient(getRegionOrDefault(), clientClass);
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

    static final String SEP = ",";

    public final String getDescription() {
        if (resources == null)
            return "";
        var description = String.join(SEP, resources
                .stream()
                .map(AWSResources::getDescription)
                .toList());
        return description;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    protected void success() {
        setResult(Result.success(this));
    }

    protected void success(String key, String value) {
        setResult(Result.success(this, key, value));
    }

    public List<T> getResources() {
        return resources;
    }

    public void setResources(List<T> resources) {
        this.resources = resources;
    }

    public String getResultType() {
        Result result = getResult();
        return result == null ? "NULL"
                : result.getType().toString();
    }
}