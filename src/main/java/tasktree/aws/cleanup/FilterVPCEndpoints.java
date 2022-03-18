package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterVPCEndpoints extends AWSFilter<VpcEndpoint> {

    private String vpcId;

    public FilterVPCEndpoints(String vpcId) {
        this.vpcId = vpcId;
    }

    private boolean match(VpcEndpoint resource) {
        var prefix = getAwsCleanupPrefix();
        var match = resource.vpcId().equals(vpcId);
        match = match || resource.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name")
                        && tag.value().startsWith(prefix));
        log().trace("Found VPC Endpoint {} {}", mark(match), resource);
        return match;
    }

    @Override
    protected List<VpcEndpoint> filterResources() {
        var client = newEC2Client();
        var resources = client.describeVpcEndpoints().vpcEndpoints();
        var matches = resources.stream().filter(this::match).toList();
        return matches;
    }

    @Override
    protected Stream<Task> mapSubtasks(VpcEndpoint resource) {
        return Stream.of(new DeleteVPCEndpoint(resource));
    }

    @Override
    protected String getResourceType() {
        return "VPC Endpoint";
    }
}

