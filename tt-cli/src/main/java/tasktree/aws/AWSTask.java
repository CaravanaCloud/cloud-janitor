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
    import java.util.ArrayList;
    import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class AWSTask<T>
        extends BaseTask {

    static final Region DEFAULT_REGION = Region.US_EAST_1;
    static final Logger log = LoggerFactory.getLogger(AWSTask.class);

    protected List<T> resources = new ArrayList<>();
    protected List<Task> subtasks = null;

    @ConfigProperty(name = "tt.aws.region", defaultValue = "us-east-1")
    String defaultRegion;

    @ConfigProperty(name = "tt.aws.cleanup.prefix", defaultValue = "prefix-to-cleanup")
    String awsCleanupPrefix;

    String resourceDescription = "";

    protected Region region;

    protected AWSClients aws = AWSClients.getInstance();

    public AWSTask() {
    }

    public AWSTask(Configuration config) {
        super(config);
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

    public void setRegion(Region region) {
        this.region = region;
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
            awstask.setDefaultRegion(getDefaultRegion());
            awstask.setRegion(_region);
            awstask.setAwsCleanupPrefix(getAwsCleanupPrefix());
        }
        return task;
    }

    private void setDefaultRegion(String defaultRegion) {
        this.defaultRegion = defaultRegion;
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

    public void setAwsCleanupPrefix(String prefix) {
        this.awsCleanupPrefix = prefix;
    }

    public String getAwsCleanupPrefix() {
        if (awsCleanupPrefix == null || awsCleanupPrefix.isEmpty()) {
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
        if (defaultRegion == null || defaultRegion.isEmpty())
            return true;
        else {
            var filter = regionName.equals(defaultRegion);
            return filter;
        }
    }

    protected Region getRegion() {
        if (region == null) {
            var defaultRegion = getDefaultRegion();
            if (defaultRegion == null || defaultRegion.isEmpty()) {
                log.warn("No region set, using default region [{}] [{}]", DEFAULT_REGION, getSimpleName());
                region = DEFAULT_REGION;
            }
            region = Region.of(defaultRegion);
        }
        return region;
    }

    @PostConstruct
    public void postConstruct() {
        log.trace("Initializing AWS Task {}", this);
        if (region == null) {
            region = Region.of(getDefaultRegion());
        }
    }

    protected String getResourceType() {
        return "AWS Resource";
    }


    protected  <T extends SdkClient> T getClient(Class<T> clientClass) {
        return aws.getClient(getRegion(), clientClass);
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
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
}