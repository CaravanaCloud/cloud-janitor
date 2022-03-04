package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class FilterRegion extends AWSFilter<Region> {

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
