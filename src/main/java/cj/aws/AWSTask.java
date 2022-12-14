package cj.aws;

import cj.BaseTask;
import cj.CJInput;
import cj.aws.sts.AWSLoadIdentitiesTask;
import cj.spi.Task;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Filter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static cj.aws.AWSInput.identity;
import static cj.aws.AWSOutput.Identities;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.awaitility.Awaitility.await;

public abstract class AWSTask
        extends BaseTask {
    static final Random rand = new Random();
    @Inject
    AWSClientsManager awsManager;
    protected AWSClients aws() {
        return awsManager.of(identity(), region());
    }

    protected AWSClients aws(Region region) {
        return awsManager.of(identity(), region);
    }

    protected AWSIdentity identity() {
        var idIn = inputAs(identity, AWSIdentity.class);
        if (idIn.isPresent())
            return idIn.get();
        var id = awsManager.defaultIdentity();
        if (id != null){
            setIdentity(id);
            return id;
        }
        throw fail("AWS Identity not found for task {}", this);
    }

    protected void setIdentity(AWSIdentity id) {
        inputs().put(identity, id);
    }

    protected <T> T create(Instance<T> instance) {
        @SuppressWarnings("redundant")
        var result = instance.get();
        return result;
    }

    protected Region region() {
        var regionIn = inputAs(AWSInput.targetRegion, Region.class);
        return regionIn.orElse(awsManager.defaultRegion());
    }

    protected Filter filter(String filterName, String filterValue) {
        return Filter.builder().name(filterName).values(filterValue).build();
    }

    @Override
    public String getContextString() {
        return String.join(" - ", getContext());
    }

    private List<String> getContext() {
        var info = identityInfo();
        if (info != null) {
            String acctAlias = "" + info.accountAlias();
            String region = "" + getRegionName();
            return List.of("aws",
                    acctAlias,
                    region);
        } else return List.of("aws");
    }

    protected AWSIdentityInfo identityInfo() {
        var id = identity();
        var info = awsManager.getInfo(id);
        return info;
    }

    private String getRegionName() {
        var region = region();
        return region.toString();
    }

    protected Duration getPollInterval() {
        return getPollInterval(30.00);
    }

    protected Duration getPollIntervalLong() {
        return getPollInterval(60.00);
    }


    protected Duration getPollInterval(double pollInterval) {
        var variance = 0.10;
        var noise = rand.nextDouble() * variance;
        var signal = 1 - noise;
        pollInterval *= signal;
        var seconds = Double.valueOf(pollInterval).longValue();
        @SuppressWarnings("redundant")
        var duration = Duration.ofSeconds(seconds);
        return duration;
    }

    protected Duration getAtMost() {
        return Duration.ofMinutes(10L);
    }

    protected Duration getAtMostLong() {
        return Duration.ofMinutes(60L);
    }

    protected void awaitUntil(Callable<Boolean> condition) {
        await().atMost(getAtMost())
                .pollInterval(getPollInterval())
                .until(condition);
    }

    protected void awaitUntilLong(Callable<Boolean> condition) {
        await().atMost(getAtMostLong())
                .pollInterval(getPollIntervalLong())
                .until(condition);
    }

    @Inject
    Instance<AWSLoadIdentitiesTask> loadAWSIdentitiesTask;

    protected List<AWSIdentity> loadAWSIdentities() {
        var task = loadAWSIdentitiesTask.get();
        submit(task);
        @SuppressWarnings("redundant")
        var identities = task.outputList(Identities, AWSIdentity.class);
        return identities;
    }



    protected String regionName(){
        var region = region();
        return region != null ? region.toString() : "";
    }

    protected String accountId(){
        var info = identityInfo();
        return info != null ? info.accountId() : "";
    }


    @Inject
    Instance<AWSRepeaterTask> iteratorInstance;

    protected <T extends Task> void forEachRegion(Instance<T> taskInstance) {
        checkArgument(taskInstance.isResolvable(), "Task instance is not resolvable");
        submitInstance(iteratorInstance, CJInput.regionTask, taskInstance);
    }

    protected <T extends Task> void forEachIdentity(Instance<T> taskInstance) {
        checkNotNull(taskInstance);
        checkArgument(taskInstance.isResolvable(), "Task instance is not resolvable");
        submitInstance(iteratorInstance, CJInput.identityTask, taskInstance);
    }

    @Override
    public void apply() {
        var id = identity();
        var awsClients = aws();
        var defaultId = awsManager.defaultIdentity();
        var defaultCreds = defaultId.toCredentialsProvider(null);
        awsClients.setCredentialsProvider(defaultCreds);
        try (var sts = awsClients.sts()){
            var taskCreds = id.toCredentialsProvider(sts);
            awsClients.setCredentialsProvider(taskCreds);
            checkArgument(id.equals(identity()));
            applyIdentity(id);
        }catch(Exception ex){
            throw fail("Failed to invoke aws task with STS client");
        }

    }

    protected void applyIdentity(AWSIdentity id){
        log().warn("applyIdentity not implemented for {}", this);
    }
}