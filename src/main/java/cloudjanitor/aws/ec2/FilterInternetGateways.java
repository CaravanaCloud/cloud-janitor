package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import software.amazon.awssdk.services.ec2.model.InternetGateway;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.stream.Stream;

import static cloudjanitor.Output.AWS.*;

@Dependent
public class FilterInternetGateways extends AWSFilter {

    private boolean match(InternetGateway resource) {
        var prefix = aws().config().filterPrefix();
        var match = true;
        if (prefix.isPresent()){
            var nameMatch = resource.tags().stream()
                    .anyMatch(tag -> tag.key().equals("Name")
                            && tag.value().startsWith(prefix.get()));

            match = nameMatch;
        }

        var vpcId = input(Input.AWS.TargetVpcId);
        if (vpcId.isPresent()){
            var vpcMatch = resource.attachments().stream().anyMatch(
                    vpc -> vpc.vpcId().equals(vpcId));
             match = match && vpcMatch;
        }

        log().trace("Found Internet Gateway {} {}", matchMark(match), resource);
        return match;
    }

    @Override
    public void apply() {
        var client = aws().newEC2Client(getRegion());
        var resources = client.describeInternetGateways().internetGateways();
        var matches = resources.stream().filter(this::match).toList();
        success(InternetGatewayMatch, matches);
    }
}
