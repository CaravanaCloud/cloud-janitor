package tasktree.aws.cleanup;

import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

public class FilterRegions extends AWSFilter< Region> {

    @Override
    public List<Region> filterResources() {
            var regions = aws.newEC2Client(getRegion()).describeRegions()
                    .regions()
                    .stream()
                    .map(software.amazon.awssdk.services.ec2.model.Region::regionName)
                    .toList();
            var matches = regions
                    .stream()
                    .filter(this::filterRegion)
                    .map(Region::of)
                    .toList();
            return matches;
    }

    @Override
    public Stream<Task> mapSubtasks(Region region) {
        var filterRegion = new FilterRegion (region);
        return Stream.of(filterRegion);
    }

    @Override
    protected String getResourceType() {
        return "Regions List";
    }
}
