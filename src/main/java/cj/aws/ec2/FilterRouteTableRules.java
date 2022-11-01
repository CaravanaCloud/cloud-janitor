package cj.aws.ec2;

import cj.Input;
import cj.Output;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import cj.aws.AWSFilter;

import javax.enterprise.context.Dependent;

@Dependent
public class FilterRouteTableRules extends AWSFilter {

    private boolean match(RouteTable resource) {
        var vpcId = inputString(Input.aws.targetVPCId);
        var match = true;
        if (vpcId.isPresent()){
            match = match && resource.vpcId().equals(vpcId);
        }
        var prefix = aws().config().filterPrefix();
        if(prefix.isPresent()) {
            match = match && resource.tags().stream()
                    .anyMatch(tag -> tag.key().equals("Name")
                            && tag.value().startsWith(prefix.get()));
        }
        trace("Found Route Table {} {}", matchMark(match), resource);
        return match;
    }

    public void apply() {
        var client = aws().ec2();
        var resources = client.describeRouteTables().routeTables();
        var matches = resources.stream().filter(this::match).toList();
        success(Output.AWS.RouteTableRulesMatch, matches);
    }
}

