package cj.aws.filter;

import cj.aws.AWSInput;
import cj.aws.AWSTask;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.aws.AWSOutput.RegionMatches;

@Dependent
public class AWSFilterIdentityTask extends AWSTask {
    @Inject
    FilterRegions filterRegions;

    @Inject
    Instance<FilterRegion> filterRegion;

    @Override
    public void apply() {
        info("Filtering AWS identity - {}", identity());
        var regions = submit(filterRegions)
                .outputList(RegionMatches, Region.class);
        forEach(regions, this::filterRegion);
    }



    private void filterRegion(Region region) {
        submitInstance(filterRegion, AWSInput.targetRegion, region);
    }
}
