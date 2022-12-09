package cj.aws.filter;

import cj.aws.AWSFilter;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

import javax.enterprise.context.Dependent;
import java.util.Comparator;

import static cj.aws.AWSOutput.RegionMatches;
@Dependent
public class FilterRegions extends AWSFilter {

    @Override
    public void apply() {
        try(var ec2 = aws().ec2()){
            filterRegions(ec2);
        }
    }

    private void filterRegions(Ec2Client ec2) {
        var allRegions = ec2.describeRegions()
                .regions()
                .stream()
                .map(r -> Region.of(r.regionName()))
                .toList();

        var matchRegions = allRegions.stream();
        var allowRegionsIn = aws().config().regions();
        if (allowRegionsIn.isPresent()) {
            var allowRegions = allowRegionsIn.get();
            matchRegions = matchRegions.filter(r -> allowRegions.contains(r.id()));
        }
        var matches = matchRegions
                .sorted(Comparator.comparing(Region::id))
                .toList();
        info("Matched {}/{} regions. {}", matches.size(), allRegions.size(), matches);
        success(RegionMatches, matches);
    }


}

