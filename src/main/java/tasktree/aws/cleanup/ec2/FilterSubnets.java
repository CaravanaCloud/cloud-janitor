package tasktree.aws.cleanup.ec2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import tasktree.aws.AWSFilter;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterSubnets extends AWSFilter<Subnet> {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);
    private String vpcId;

    public FilterSubnets(String vpcId) {
        this.vpcId = vpcId;
    }

    private boolean match(Subnet net) {
        var prefix = getAwsCleanupPrefix();
        var match = net.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        log.trace("Found Subnet {} {}", mark(match), net);
        return match;
    }

    @Override
    protected List<Subnet> filterResources() {
        var ec2 = newEC2Client();
        var describeNets = DescribeSubnetsRequest.builder().build();
        var nets = ec2.describeSubnets(describeNets).subnets().stream();
        var matches = nets.filter(this::match).toList();
        return matches;
    }

    @Override
    protected Stream<Task> mapSubtasks(Subnet subnet) {
        return Stream.of(new DeleteSubnet(subnet));
    }

    @Override
    protected String getResourceType() {
        return "Subnets";
    }

}
