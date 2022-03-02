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
        var prefix = getConfig().getAwsCleanupPrefix();
        var match = resource.vpcId().equals(vpcId);
        match = match  || resource.tagSet().stream()
                .anyMatch(tag -> tag.key().equals("Name")
                        && tag.value().startsWith(prefix));
        log().trace("Found Network Interface {} {}", mark(match), resource);
        return match;
    }

    private List<NetworkInterface> filterResources() {
        var client = newEC2Client();
        var resources = client.describeNetworkInterfaces().networkInterfaces();
        var matches = resources.stream().filter(this::match).toList();
        log().info("Matched [{}] Network Interfaces in region [{}] [{}]", matches.size(), getRegion(), matches);
        return matches;
    }


    @Override
    public void run() {
        var resources = filterResources();
        addAllTasks(deleteTasks(resources));
    }


    private Stream<Task> deleteTasks(List<NetworkInterface> subnets) {
        return subnets.stream().map(this::deleteTask);
    }


    private Task deleteTask(NetworkInterface resource) {
        return new DeleteNetworkInterface(getConfig(), resource);
    }

}