package cj.aws.filter;

import cj.aws.AWSIdentity;
import cj.aws.AWSInput;
import cj.aws.AWSTask;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

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
        submitInstance(filterIdTask, AWSInput.identity, awsIdentity);
    }
}
