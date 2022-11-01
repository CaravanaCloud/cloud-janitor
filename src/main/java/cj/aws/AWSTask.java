package cj.aws;

import cj.BaseTask;
import cj.Input;
import cj.Output;
import cj.aws.sts.CallerIdentity;
import cj.aws.sts.GetCallerIdentityTask;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Filter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static cj.Input.aws.identity;
import static org.awaitility.Awaitility.await;

public abstract class   AWSTask
        extends BaseTask {
    static final Random rand = new Random();

    static AWSIdentity defaultIdentity = null;

    @Inject
    Instance<GetCallerIdentityTask> getCallerIdInstance;
    private String accountName;
    private String regionName;

    public AWSClients aws(){
        var identity = getIdentity();
        var config = getConfig().aws();
        var region = getRegion();
        return AWSClients.of(config, identity, region);
    }

    protected AWSIdentity getIdentity() {
        var id = inputAs(identity, AWSIdentity.class);
        if (id.isPresent()){
            var awsId = id.get();
            return awsId;
        }
        else{
            if (defaultIdentity == null) {
                logger().info("Loading default AWS Identity for task.");
                defaultIdentity = loadDefaultAWSIdentity();
            }
            return defaultIdentity;
        }
    }

    protected void setIdentity(AWSIdentity id){
        getInputs().put(identity, id);
    }

    private synchronized AWSIdentity loadDefaultAWSIdentity() {
        var id = DefaultAWSIdentity.of();
        var callerIdTask = (BaseTask) getCallerIdInstance.get()
                .withInput(identity, id);
        submit(callerIdTask);
        var callerId = callerIdTask.outputAs(Output.aws.CallerIdentity, CallerIdentity.class);
        id = id.withCallerIdentity(callerId);
        setIdentity(id);
        return id;
    }

    protected <T> T create(Instance<T> instance){
        var result = instance.get();
        return result;
    }

    protected Region getRegion(){
        var regionIn = inputAs(Input.aws.targetRegion, Region.class);
        if (regionIn.isEmpty()){
            var regionName = getConfig().aws().defaultRegion();
            return Region.of(regionName);
        }
        return regionIn.get();
    }

    protected Filter filter(String filterName, String filterValue) {
        return Filter.builder().name(filterName).values(filterValue).build();
    }

    @Override
    public boolean isWrite() {
        return false;
    }

    @Override
    protected String getContextString() {
        return String.join(" - ", getContext());
    }

    private List<String> getContext() {
        var id = getIdentity();
        if (id != null){
            String acctName = getAccountName();
            String region = getRegionName();
            if (acctName == null || region == null){
                System.out.println();
            }
            return List.of("aws",
                    acctName,
                    region);
        }
        else return List.of("aws");
    }

    private String getAccountName() {
        if(accountName == null){
            accountName = lookupAccountName();
        }
        return accountName;
    }

    private String lookupAccountName() {
        var identityIn = getIdentity();
        if (identityIn == null){
            System.out.println("Could not find AWS identity for task");
            return "? unknown account id ?";
        }else{
            return identityIn.getAccountName();
        }
    }

    private String getRegionName() {
        if (regionName == null){
            regionName = aws().getRegion().toString();
        }
        return regionName;
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
        var duration = Duration.ofSeconds(seconds);
        return duration;
    }

    protected Duration getAtMost() {
        return Duration.ofMinutes(10L);
    }

    protected Duration getAtMostLong() {
        return Duration.ofMinutes(60L);
    }

    protected void awaitUntil(Callable<Boolean> condition){
        await().atMost(getAtMost())
                .pollInterval(getPollInterval())
                .until(condition);
    }

    protected void awaitUntilLong(Callable<Boolean> condition){
        await().atMost(getAtMostLong())
                .pollInterval(getPollIntervalLong())
                .until(condition);
    }

}