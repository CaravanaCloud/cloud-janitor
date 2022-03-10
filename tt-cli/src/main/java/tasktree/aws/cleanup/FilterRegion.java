package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

public class FilterRegion extends AWSFilter<Region> {

    public FilterRegion(Region region) {
        this.region = region;
    }

    @Override
    protected List<Region> filterResources() {
        return List.of(region);
    }

    @Override
    protected Stream<Task> mapSubtasks(Region region) {
        return Stream.of(new FilterVPCs(),
                new FilterTargetGroups(),
                new FilterLoadBalancersV2(),
                new FilterNATGateways(),
                new FilterAddresses(),
                new FilterInstances()
        );
    }

    @Override
    protected String getResourceType() {
        return "Region";
    }
}
