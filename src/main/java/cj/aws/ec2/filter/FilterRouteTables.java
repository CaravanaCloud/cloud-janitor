package cj.aws.ec2.filter;

import cj.Input;
import cj.aws.AWSFilter;
import cj.aws.AWSInput;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import javax.enterprise.context.Dependent;

import static cj.Output.aws.RouteTablesMatch;

@Dependent
public class FilterRouteTables extends AWSFilter {

    private boolean match(RouteTable resource) {
        var match = true;

        var vpcId = input(AWSInput.targetVPCId);
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
        trace("Found Route Table {} {}  ", matchMark(match), resource);
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

