package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.RouteTable;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterRouteTables extends AWSFilter<RouteTable> {

    private String vpcId;

    public FilterRouteTables(String vpcId) {
        this.vpcId = vpcId;
    }

    private boolean match(RouteTable resource) {
        var prefix = getAwsCleanupPrefix();
        var match = resource.vpcId().equals(vpcId);
        match = match || resource.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name")
                        && tag.value().startsWith(prefix));
        log().debug("Found Route Table {} {}", mark(match), resource);
        return match;
    }

    protected List<RouteTable> filterResources() {
        var client = newEC2Client();
        var resources = client.describeRouteTables().routeTables();
        var matches = resources.stream().filter(this::match).toList();
        log().info("Matched [{}] Route Tables in region [{}]", matches.size(), getRegion(), matches);
        return matches;
    }


    @Override
    public void run() {
        var resources = filterResources();
        addAllTasks(deleteTasks(resources));
    }


    private Stream<Task> deleteTasks(List<RouteTable> subnets) {
        return subnets.stream().map(this::deleteTask);
    }


    private Task deleteTask(RouteTable resource) {
        return new DeleteRouteTable(getConfig(), resource);
    }

}

