package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.RouteTable;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterSecurityGroups extends AWSFilter<SecurityGroup> {

    private String vpcId;

    public FilterSecurityGroups(Configuration config, String vpcId) {
        super(config);
        this.vpcId = vpcId;
    }

    private boolean match(SecurityGroup resource) {
        var notDefault = ! resource.groupName().equals("default");
        var targetVPC = resource.vpcId().equals(vpcId);
        var match = notDefault && targetVPC;
        log().debug("Found Security Group {} {}", mark(match), resource);
        return match;
    }

    private List<SecurityGroup> filterResources() {
        var client = newEC2Client();
        var resources = client.describeSecurityGroups().securityGroups();
        var matches = resources.stream().filter(this::match).toList();
        log().info("Matched {} Security Groups in region [{}]", matches.size(), getRegion());
        return matches;
    }


    @Override
    public void run() {
        var resources = filterResources();
        dryPush(deleteTasks(resources));
    }


    private Stream<Task> deleteTasks(List<SecurityGroup> resources) {
        return resources.stream().map(this::deleteTask);
    }


    private Task deleteTask(SecurityGroup resource) {
        return new DeleteSecurityGroup(getConfig(), resource);
    }

}

