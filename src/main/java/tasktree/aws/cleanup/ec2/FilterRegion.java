package tasktree.aws.cleanup.ec2;

import software.amazon.awssdk.regions.Region;
import tasktree.aws.AWSFilter;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterRegion extends AWSFilter<Region> {

    public FilterRegion(Region region) {
        super(region);
    }

    @Override
    protected List<Region> filterResources() {
        return List.of(region);
    }

    @Override
    protected Stream<Task> mapSubtasks(Region region) {
        return Stream.of(new FilterVPCs());
    }

    @Override
    protected String getResourceType() {
        return "Region";
    }
}
