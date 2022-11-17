package cj.aws.filter;

import cj.aws.AWSFilter;
import cj.aws.AWSInput;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.Dependent;

@Dependent
public class FilterRegion extends AWSFilter {

    @Override
    public void apply() {
        var region = getInput(AWSInput.targetRegion, Region.class);
        info("Filtering region {}", region);
    }
}
