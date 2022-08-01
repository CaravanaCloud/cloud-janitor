package cloudjanitor.aws.ec2;

import cloudjanitor.BaseTask;
import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.aws.AWSIdentity;
import cloudjanitor.aws.DefaultAWSIdentity;
import cloudjanitor.aws.cleanup.CleanupRegions;
import cloudjanitor.aws.sts.GetCallerIdentityTask;
import cloudjanitor.aws.sts.LoadAWSIdentitiesTask;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import static cloudjanitor.Input.AWS.Identity;
import static cloudjanitor.Output.AWS.Identities;

@Named("cleanup-aws")
@Dependent
public class CleanupAWS extends BaseTask {
    @Inject
    LoadAWSIdentitiesTask loadIdsTask;

    @Inject
    Instance<CleanupAWSIdentity> cleanupAWSIdentityInstance;

    private void setDefaultIdentity() {
        var identity = DefaultAWSIdentity.of();
        getInputs().put(Identity, identity);
    }

    @Override
    public void apply() {
        var ids = loadIdsTask.outputList(Identities, AWSIdentity.class);
        debug("Cleaning up {} AWS identities. {}", ids.size(), ids);
        var tasks = ids.stream().map(this::toTask).toList();
        submitAll(tasks);
    }

    private Task toTask(AWSIdentity awsIdentity) {
        return cleanupAWSIdentityInstance.get()
                .withInput(Identity, awsIdentity);
    }

    @Override
    public Task getDependency() {
        return loadIdsTask;
    }
}
