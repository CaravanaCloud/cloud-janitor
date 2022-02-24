package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;

public class FilterRegion extends AWSFilter<Region> {
    public FilterRegion(Configuration config, Region region) {
        super(config, region);
    }

    @Override
    public void run() {
        log().info("Filtering region {}", getRegion());
        addAllTasks(new FilterInstances(),
                new FilterNATGateways(),
                new FilterAddresses(),
                new FilterLoadBalancersV2(),
                new FilterTargetGroups(),
                new FilterVPCs());
    }
}
