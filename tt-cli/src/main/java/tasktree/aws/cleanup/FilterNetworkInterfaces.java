package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterNetworkInterfaces extends AWSFilter<NetworkInterface> {
    String vpcId;

    public FilterNetworkInterfaces(String vpcId) {
        this.vpcId = vpcId;
    }

    private boolean match(NetworkInterface resource) {
        var prefix = getAwsCleanupPrefix();
        var match = resource.vpcId().equals(vpcId);
        match = match  || resource.tagSet().stream()
                .anyMatch(tag -> tag.key().equals("Name")
                        && tag.value().startsWith(prefix));
        log().trace("Found Network Interface {} {}", mark(match), resource);
        return match;
    }

    @Override
    protected List<NetworkInterface> filterResources() {
        var client = newEC2Client();
        var resources = client.describeNetworkInterfaces().networkInterfaces();
        var matches = resources.stream().filter(this::match).toList();
        return matches;
    }

    @Override
    protected Stream<Task> mapSubtasks(NetworkInterface resource) {
        return Stream.of(new DeleteNetworkInterface(resource));
    }

    @Override
    protected String toString(NetworkInterface networkInterface) {
        return networkInterface.networkInterfaceId();
    }

    @Override
    protected String getResourceType() {
        return "Network Interface";
    }
}