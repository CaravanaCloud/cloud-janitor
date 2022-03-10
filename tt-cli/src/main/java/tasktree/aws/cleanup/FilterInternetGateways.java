package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.InternetGateway;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterInternetGateways extends AWSFilter<InternetGateway> {

    private String vpcId;

    public FilterInternetGateways(String vpcId) {
        this.vpcId = vpcId;
    }

    private boolean match(InternetGateway resource) {
        var prefix = getAwsCleanupPrefix();
        var match = resource.attachments().stream().anyMatch(vpc -> vpc.vpcId().equals(vpcId));
        match = match || resource.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name")
                        && tag.value().startsWith(prefix));
        log().trace("Found Internet Gateway {} {}", mark(match), resource);
        return match;
    }

    @Override
    protected List<InternetGateway> filterResources() {
        var client = newEC2Client();
        var resources = client.describeInternetGateways().internetGateways();
        var matches = resources.stream().filter(this::match).toList();
        return matches;
    }

    @Override
    protected Stream<Task> mapSubtasks(InternetGateway igw) {
        return Stream.of(new DeleteInternetGateway(igw));
    }

    @Override
    protected String toString(InternetGateway internetGateway) {
        return internetGateway.internetGatewayId();
    }

    @Override
    protected String getResourceType() {
        return "Internet Gateways";
    }
}

