package cloudjanitor.aws;

import cloudjanitor.Input;
import cloudjanitor.LogConstants;
import cloudjanitor.Output;
import cloudjanitor.aws.sts.CallerIdentity;
import cloudjanitor.aws.sts.GetCallerIdentityTask;
import software.amazon.awssdk.regions.Region;
import cloudjanitor.BaseTask;
import software.amazon.awssdk.services.ec2.model.Filter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

import static cloudjanitor.Input.AWS.Identity;

public abstract class AWSTask
        extends BaseTask
        implements LogConstants {

    private String accountName;
    private String regionName;

    public AWSClients aws(){
        var identity = getIdentity();
        var config = getConfig().aws();
        var region = getRegion();
        return AWSClients.of(config, identity, region);
    }

    protected AWSIdentity getIdentity() {
        var id = inputAs(Identity, AWSIdentity.class);
        if (id.isPresent()){
            var awsId = id.get();
            return awsId;
        }
        else{
            logger().warn("Could not load identity from task input. Using default AWS Identity...");
            var defaultId = LoadDefaultAWSIdentity();
            return defaultId;
        }
    }

    @Inject
    Instance<GetCallerIdentityTask> getCallerIdInstance;

    private AWSIdentity LoadDefaultAWSIdentity() {
        var id = DefaultAWSIdentity.of();
        var callerIdTask = (BaseTask) getCallerIdInstance.get()
                .withInput(Identity, id);
        submit(callerIdTask);
        var callerId = callerIdTask.outputAs(Output.AWS.CallerIdentity, CallerIdentity.class);
        id = id.withCallerIdentity(callerId);
        setIdentity(id);
        return id;
    }

    protected void setIdentity(AWSIdentity id){
        getInputs().put(Identity, id);
    }


    protected <T> T create(Instance<T> instance){
        var result = instance.get();
        return result;
    }

    protected Region getRegion(){
        var regionIn = inputAs(Input.AWS.TargetRegion, Region.class);
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
                System.out.println("");
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
}