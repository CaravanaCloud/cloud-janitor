package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;
import java.util.function.Supplier;

public class FilterNATGateways extends AWSTask {

    @Override
    public void run() {
        var ec2 = newEC2Client();
        var result = ec2.describeNatGateways()
                .natGateways()
                .stream()
                .filter(this::match)
                .toList();
        log().info("Matched {} NAT gateways", result.size());
    }

    public boolean match(NatGateway nat) {
        var match = nameMatches(nat, getConfig().getAwsCleanupPrefix());
        var mark = match ? "!" : "o";
        log().debug("NAT gateway {} {}", mark, nat);
        return match;
    }

    private boolean nameMatches(NatGateway nat, String prefix) {
        return nat.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
    }
}
