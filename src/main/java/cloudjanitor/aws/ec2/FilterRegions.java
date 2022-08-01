package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import software.amazon.awssdk.regions.Region;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.stream.Stream;

import static cloudjanitor.Output.AWS.RegionMatches;

@Dependent
public class FilterRegions extends AWSFilter {

    @Override
    public void apply() {
            var regions = aws().ec2().describeRegions()
                    .regions()
                    .stream()
                    .map(software.amazon.awssdk.services.ec2.model.Region::regionName)
                    .toList();
            var matches = regions
                    .stream()
                    .filter(this::filterRegion)
                    .map(Region::of)
                    .toList();
        info("Matched {}/{} regions. {}", matches.size(), regions.size(), matches);
        success(RegionMatches, matches);
    }

    private boolean filterRegion(String region) {
        var match = true;
        var regions = aws().config().regions();
        if (regions.isPresent()){
            match = regions.get().contains(region);
        }
        return match;
    }

}

