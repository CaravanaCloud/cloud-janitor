package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.NatGateway;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterNATGateways extends AWSFilter<NatGateway> {

    @Override
    public void run() {
        var nats = describeNatGateways().stream().filter(this::match);
        addAllTasks(terminateNatGateways(nats));
    }

    private Stream<Task> terminateNatGateways(Stream<NatGateway> nats) {
        return Stream.of(new DeleteNATGateways(getConfig(), nats.toList())) ;
    }

    private List<NatGateway> describeNatGateways() {
        var ec2 = newEC2Client();
        return ec2.describeNatGateways()
                .natGateways();
    }

    public boolean match(NatGateway nat) {
        var match = nameMatches(nat, getConfig().getAwsCleanupPrefix());
        var mark = match ? "x" : "o";
        log().debug("Found NAT gateway {} {}", mark, nat);
        return match;
    }

    private boolean nameMatches(NatGateway nat, String prefix) {
        return nat.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
    }
}
