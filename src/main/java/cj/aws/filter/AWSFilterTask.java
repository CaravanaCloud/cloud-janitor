package cj.aws.filter;

import cj.Input;
import cj.aws.AWSIdentity;
import cj.aws.AWSTask;
import cj.aws.sts.LoadAWSIdentitiesTask;
import cj.spi.Task;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Dependent
@Named("aws-filter")
public class AWSFilterTask extends AWSTask {
    @Inject
    Instance<AWSFilterIdentityTask> filterIdTask;

    @Override
    public void apply() {
        var identities = loadAWSIdentities();
        info("Filtering {} AWS identities: {}", identities.size(), identities);
        identities.forEach(this::filter);
    }

    private void filter(AWSIdentity awsIdentity) {
        submitInstance(filterIdTask, Input.aws.identity, awsIdentity);
    }
}
