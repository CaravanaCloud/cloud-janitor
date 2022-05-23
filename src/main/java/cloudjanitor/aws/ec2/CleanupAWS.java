package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.aws.sts.GetCallerIdentityTask;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("cleanup-aws")
@Dependent
public class CleanupAWS extends AWSFilter {
    @Inject
    GetCallerIdentityTask getCaller;

    @Override
    public void runSafe() {
        var account = Output.AWS_ACCOUNT.findString(getCaller);
        if(account.isPresent()){
            log().info("Cleanup AWS Account {}.", account.get());
        }else{
            log().warn("Could not find AWS account to cleanup.");
        }
    }

    @Override
    public List<Task> getDependencies() {
        return List.of(getCaller);
    }

    /*
    @Override
    protected List<AWSAccount> filterResources() {
        var sts = getClient(StsClient.class);
        var accountId = sts.getCallerIdentity().account();
        setResourceDescription(accountId);
        return List.of(new AWSAccount(accountId));
    }

    @Override
    protected Stream<Task> mapSubtasks(AWSAccount accountId) {
        return Stream.of(
                new FilterRegions(),
                new FilterRecords());
    }

    @Override
    protected String getResourceType() {
        return "AWS Account";
    }

     */

}
