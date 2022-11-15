package cj.aws.ec2.cleanup;

import cj.aws.AWSIdentity;
import cj.aws.AWSTask;
import cj.aws.cleanup.CleanupRegions;
import cj.aws.sts.GetCallerIdentityTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static cj.Input.aws.identity;

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
