package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import java.util.stream.Stream;

import static cloudjanitor.Output.AWS.NetworkINterfacesMatch;

public class FilterNetworkInterfaces extends AWSFilter {

    private boolean match(NetworkInterface resource) {
        var match = true;
        var vpcId = inputString(Input.AWS.TargetVpcId);
        if (vpcId.isPresent()){
            match = match && resource.vpcId().equals(vpcId);
        }
        var prefix = aws().config().filterPrefix();
        if (prefix.isPresent()) {
            match = match || resource.tagSet().stream()
                    .anyMatch(tag -> tag.key().equals("Name")
                            && tag.value().startsWith(prefix.get()));
        }
        log().trace("Found Network Interface {} {}", matchMark(match), resource);
        return match;
    }

    @Override
    public void apply() {
        var client = aws().ec2();
        var resources = client.describeNetworkInterfaces().networkInterfaces();
        var matches = resources.stream().filter(this::match).toList();
        success(NetworkINterfacesMatch,  matches);
    }
}