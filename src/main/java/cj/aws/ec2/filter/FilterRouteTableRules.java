package cj.aws.ec2.filter;

import cj.Output;
import cj.aws.AWSFilter;
import cj.aws.AWSInput;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import javax.enterprise.context.Dependent;
import static cj.aws.AWSOutput.*;

@Dependent
public class FilterRouteTableRules extends AWSFilter {

    private boolean match(RouteTable resource) {
        var vpcId = inputString(AWSInput.targetVPCId);
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
        success(RouteTableRulesMatch, matches);
    }
}

