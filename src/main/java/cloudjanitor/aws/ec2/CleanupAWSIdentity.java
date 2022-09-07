package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSIdentity;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.aws.cleanup.CleanupRegions;
import cloudjanitor.aws.sts.GetCallerIdentityTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static cloudjanitor.Input.AWS.identity;

@Dependent
public class CleanupAWSIdentity extends AWSTask {
    @Inject
    CleanupRegions cleanupRegions;

    @Inject
    GetCallerIdentityTask callerId;

    @Override
    public void apply() {
        var id = getInput(identity, AWSIdentity.class);
        info("Cleaning up  AWS identity: {}", id);
        submitAll(
                callerId,
                cleanupRegions);
        success();
    }


}
