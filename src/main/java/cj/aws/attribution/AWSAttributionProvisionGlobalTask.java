package cj.aws.attribution;

import cj.aws.AWSTask;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

//TODO: Multi-identity support
@Dependent
@Named("aws-attribution-provision")
public class AWSAttributionProvisionGlobalTask extends AWSTask {
    @Inject
    Instance<AWSAttributionRegionTask> getTrail;

    @Override
    public void apply() {
        trace("started creating attribution resources");
        forEachRegion(getTrail);
        debug("done creating attribution resources");
    }
}
