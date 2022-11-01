package cj.aws.ec2;

import cj.Input;
import software.amazon.awssdk.services.ec2.model.InternetGateway;
import cj.aws.AWSFilter;

import javax.enterprise.context.Dependent;

import static cj.Output.AWS.*;

@Dependent
public class FilterInternetGateways extends AWSFilter {

    private boolean match(InternetGateway resource) {
        var match = true;

        var prefix = aws().config().filterPrefix();
        if (prefix.isPresent()){
            var nameMatch = resource.tags().stream()
                    .anyMatch(tag -> tag.key().equals("Name")
                            && tag.value().startsWith(prefix.get()));

            match = nameMatch;
        }

        var vpcId = inputAs(Input.aws.targetVPCId, String.class);
        if (vpcId.isPresent()){
            var vpcMatch = resource.attachments().stream().anyMatch(
                    vpc -> vpc.vpcId().equals(vpcId.get()));
             match = match && vpcMatch;
        }

        trace("Found Internet Gateway {} {}", matchMark(match), resource);
        return match;
    }

    @Override
    public void apply() {
        var client = aws().ec2(getRegion());
        var resources = client.describeInternetGateways().internetGateways();
        var matches = resources.stream().filter(this::match).toList();
        success(InternetGatewayMatch, matches);
    }
}
