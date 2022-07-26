package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.aws.CleanupRegions;
import cloudjanitor.aws.sts.GetCallerIdentityTask;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

@Named("cleanup-aws")
@Dependent
public class CleanupAWS extends AWSFilter {
    @Inject
    GetCallerIdentityTask getCallerIdentityTask;

    @Inject
    CleanupRegions cleanupRegions;

    @Override
    public void apply() {
        var account = getCallerIdentityTask.outputString(Output.AWS.Account);
        if(account.isPresent()){
            log().info("Cleanup AWS Account {}", account.get());

        }else{
            log().warn("Could not find AWS account to cleanup");
        }
    }

    @Override
    public Task getDependency() {
        return getCallerIdentityTask;
    }
}
