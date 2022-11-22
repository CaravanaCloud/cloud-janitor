package cj.aws.filter;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.aws.AWSIdentity;
import cj.aws.AWSInput;
import cj.aws.AWSTask;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.TaskMaturity.Level.experimental;

@Dependent
@Named("aws-filter")
@TaskDescription("Search for AWS resources in multiple accounts")
@TaskMaturity(experimental)
@SuppressWarnings("unused")
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
