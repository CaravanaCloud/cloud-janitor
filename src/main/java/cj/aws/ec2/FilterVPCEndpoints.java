package cj.aws.ec2;

import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import cj.aws.AWSFilter;

import javax.enterprise.context.Dependent;

import static cj.Input.AWS.targetVPCId;
import static cj.Output.AWS.VPCEndpointsMatch;

@Dependent
public class FilterVPCEndpoints extends AWSFilter {

    private boolean match(VpcEndpoint resource) {
        var vpcId = inputString(targetVPCId);
        var match = true;
        if (vpcId.isPresent()){
            match = match && resource.vpcId().equals(vpcId.get());
        }
        var prefix = aws().config().filterPrefix();
            if (prefix.isPresent()) {
                match = match || resource.tags().stream()
                        .anyMatch(tag -> tag.key().equals("Name")
                                && tag.value().startsWith(prefix.get()));
            }
        return match;
    }

    @Override
    public void apply() {
        var client = aws().ec2();
        var resources = client.describeVpcEndpoints().vpcEndpoints();
        var matches = resources.stream().filter(this::match).toList();
        debug("Matched {}/{} vpc endpoints", matches.size(), resources.size());
        success(VPCEndpointsMatch, matches);
    }

}

