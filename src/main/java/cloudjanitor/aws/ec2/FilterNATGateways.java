package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.ec2.model.NatGateway;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterNATGateways extends AWSFilter<NatGateway> {

    @Override
    protected List<NatGateway> filterResources() {
        var ec2 = aws().newEC2Client(getRegion());
        return ec2.describeNatGateways()
                .natGateways()
                .stream()
                .filter(this::filter)
                .toList();
    }

    private boolean filter(NatGateway natGateway) {
        var state = natGateway.state().toString();
        var skip =  state.equals("deleted");
        return !skip;
    }

    @Override
    protected Stream<Task> mapSubtasks(NatGateway natGateway) {
        return Stream.of(new DeleteNATGateway(natGateway));
    }

    public boolean match(NatGateway nat) {
        var match = nameMatches(nat, getAwsCleanupPrefix());
        var mark = match ? "x" : "o";
        return match;
    }

    private boolean nameMatches(NatGateway nat, String prefix) {
        return nat.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
    }


    protected String getResourceType() {
        return "NAT Gateway";
    }
}
