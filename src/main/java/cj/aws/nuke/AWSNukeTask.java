package cj.aws.nuke;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.aws.AWSTask;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

@Dependent
@Named("aws-nuke")
@TaskDescription("Runs aws-nuke")
@TaskMaturity(TaskMaturity.Level.experimental)
public class AWSNukeTask extends AWSTask {
    @Inject
    Instance<AWSNukeAccountTask> nuke;

    @Override
    public void apply() {
        forEachIdentity(nuke);
    }
}
