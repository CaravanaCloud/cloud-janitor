package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.stream.Stream;

import static cloudjanitor.Output.AWS.NatGatewaysMatch;

@Dependent
public class FilterNATGateways extends AWSFilter {


    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var natgws = ec2.describeNatGateways()
                .natGateways()
                .stream()
                .filter(this::filter)
                .toList();
        success(NatGatewaysMatch, natgws);
    }

    private boolean filter(NatGateway natGateway) {
        var state = natGateway.state().toString();
        var skip =  state.equals("deleted");
        return !skip;
    }

    public boolean match(NatGateway nat) {
        var match = true;
        var prefix = aws().config().filterPrefix();
        if (prefix.isPresent()){
            match = nameMatches(nat, prefix.get());
        }
        return match;
    }

    private boolean nameMatches(NatGateway nat, String prefix) {
        return nat.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
    }
}
