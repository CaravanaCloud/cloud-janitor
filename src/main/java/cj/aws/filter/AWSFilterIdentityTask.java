package cj.aws.filter;

import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterRegions;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class AWSFilterIdentityTask extends AWSTask {
    @Inject
    FilterRegions filterRegions;

    @Override
    public void apply() {
        info("Filtering AWS identity");
        submit(filterRegions);
    }
}
