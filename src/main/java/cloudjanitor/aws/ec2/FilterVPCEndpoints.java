package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.stream.Stream;

import static cloudjanitor.Input.AWS.TargetVpcId;
import static cloudjanitor.Output.AWS.VPCEndpointsMatch;

@Dependent
public class FilterVPCEndpoints extends AWSFilter {

    private boolean match(VpcEndpoint resource) {
        var vpcId = inputString(TargetVpcId);
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
        log().trace("Found VPC Endpoint {} {}", matchMark(match), resource);
        return match;
    }

    @Override
    public void apply() {
        var client = aws().ec2();
        var resources = client.describeVpcEndpoints().vpcEndpoints();
        var matches = resources.stream().filter(this::match).toList();
        success(VPCEndpointsMatch, matches);
    }

}

