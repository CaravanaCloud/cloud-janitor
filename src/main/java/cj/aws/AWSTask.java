package cj.aws;

import cj.BaseTask;
import cj.CJInput;
import cj.aws.sts.AWSLoadIdentitiesTask;
import cj.spi.Task;
import com.google.common.base.Preconditions;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Filter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static cj.aws.AWSInput.identity;
import static com.google.common.base.Preconditions.*;
import static org.awaitility.Awaitility.await;
import static cj.aws.AWSOutput.*;

public abstract class AWSTask
        extends BaseTask {
    static final Random rand = new Random();
    @Inject
    AWSClientsManager aws;
    protected AWSClients aws() {
        return aws.of(identity(), region());
    }

    protected AWSClients aws(Region region) {
        return aws.of(identity(), region);
    }

    protected AWSIdentity identity() {
        var idIn = inputAs(identity, AWSIdentity.class);
        var id = (AWSIdentity) null;
        if (idIn.isEmpty()){
            id = aws.defaultIdentity();
            if (id != null)
                setIdentity(id);
        } else id = idIn.get();
        if(id == null)
            throw fail("AWS Identity not found");
        return id;
    }

    protected void setIdentity(AWSIdentity id) {
        getInputs().put(identity, id);
    }

    protected <T> T create(Instance<T> instance) {
        @SuppressWarnings("redundant")
        var result = instance.get();
        return result;
    }

    protected Region region() {
        var regionIn = inputAs(AWSInput.targetRegion, Region.class);
        return regionIn.orElse(aws.defaultRegion());
    }

    protected Filter filter(String filterName, String filterValue) {
        return Filter.builder().name(filterName).values(filterValue).build();
    }

    @Override
    public String getContextString() {
        return String.join(" - ", getContext());
    }

    private List<String> getContext() {
        var id = identity();
        if (id != null) {
            String acctAlias = "" + id.accountAlias();
            String region = "" + getRegionName();
            return List.of("aws",
                    acctAlias,
                    region);
        } else return List.of("aws");
    }

    private String getRegionName() {
        var region = region();
        return region != null ? region.toString() : "-null-region-";
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
        var identity = identity();
        return identity != null ? identity.accountId() : "";
    }


    @Inject
    Instance<AWSIteratorTask> iteratorInstance;

    protected <T extends Task> void forEachRegion(Instance<T> taskInstance) {
        checkArgument(taskInstance.isResolvable(), "Task instance is not resolvable");
        submitInstance(iteratorInstance, CJInput.regionTask, taskInstance);
    }

    protected <T extends Task> void forEachIdentity(Instance<T> taskInstance) {
        checkNotNull(taskInstance);
        checkArgument(taskInstance.isResolvable(), "Task instance is not resolvable");
        submitInstance(iteratorInstance, CJInput.identityTask, taskInstance);
    }
}