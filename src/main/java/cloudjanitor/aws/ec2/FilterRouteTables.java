package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import cloudjanitor.aws.AWSFilter;

import javax.enterprise.context.Dependent;

import static cloudjanitor.Output.AWS.RouteTablesMatch;

@Dependent
public class FilterRouteTables extends AWSFilter {

    private boolean match(RouteTable resource) {
        var match = true;

        var vpcId = inputAs(Input.AWS.TargetVpcId);
        if (vpcId.isPresent()){
            var matchVpcId = resource.vpcId();
            var targetVpcId = vpcId.get().toString();
            var vpcMatch = matchVpcId.equals(targetVpcId);
            match = match && vpcMatch;
        }

        var prefix = aws().config().filterPrefix();
        if(prefix.isPresent()){
            var prefixMatch = resource.tags().stream()
                    .anyMatch(tag -> tag.key().equals("Name")
                            && tag.value().startsWith(prefix.get()));
            match = match && prefixMatch;
        }
        log().trace("Found Route Table {} {}  ", matchMark(match), resource);
        return match;
    }

    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var resources = ec2.describeRouteTables().routeTables();
        var matches = resources.stream().filter(this::match).toList();
        success(RouteTablesMatch, matches);
    }
}

