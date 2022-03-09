package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

public class FilterRegion extends AWSFilter<Region> {

    public FilterRegion(Region region) {
        setRegion(region);
    }

    @Override
    public void run() {
        log().info("Filtering region [{}]", getRegion());
        addAllTasks(
                new FilterVPCs(),
                    new FilterTargetGroups(),
                    new FilterLoadBalancersV2(),
                    new FilterNATGateways(),
                    new FilterAddresses(),
                        new FilterInstances()
        );
    }
}
