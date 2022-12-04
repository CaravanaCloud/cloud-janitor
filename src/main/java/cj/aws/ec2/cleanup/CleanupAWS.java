package cj.aws.ec2.cleanup;

import cj.BaseTask;
import cj.aws.AWSIdentity;
import cj.aws.sts.AWSLoadIdentitiesTask;
import cj.spi.Task;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.aws.AWSOutput.*;
import static cj.aws.AWSInput.identity;

@Named("cleanup-aws")
@Dependent
public class CleanupAWS extends BaseTask {
    @Inject
    AWSLoadIdentitiesTask loadIdsTask;

    @Inject
    Instance<CleanupAWSIdentity> cleanupAWSIdentityInstance;

    @Override
    public void apply() {
        var ids = loadIdsTask.outputList(Identities, AWSIdentity.class);
        debug("Cleaning up {} AWS identities. {}", ids.size(), ids);
        var tasks = ids.stream().map(this::toTask).toList();
        submitAll(tasks);
    }

    private Task toTask(AWSIdentity awsIdentity) {
        return cleanupAWSIdentityInstance.get()
                .withInput(identity, awsIdentity);
    }

    @Override
    public Task getDependency() {
        return loadIdsTask;
    }
}
