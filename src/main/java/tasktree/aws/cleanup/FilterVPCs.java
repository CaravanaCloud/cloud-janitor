package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterVPCs extends AWSFilter<Vpc> {
    static final Logger log = LoggerFactory.getLogger(FilterVPCs.class);

    private boolean match(Vpc vpc) {
        var prefix = getAwsCleanupPrefix();
        var match = vpc.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        return match;
    }

    private List<Vpc> findAll(){
        var ec2 = newEC2Client();
        var request = DescribeVpcsRequest.builder().build();
        var resources = ec2.describeVpcs(request).vpcs();
        return resources;
    }

    @Override
    protected List<Vpc> filterResources() {
        var matches = findAll().stream().filter(this::match).toList();
        return matches;
    }

    @Override
    public Stream<Task> mapSubtasks(Vpc vpc) {
        var vpcId = vpc.vpcId();
        return Stream.of(
                new DeleteVpc(vpc)
        );
    }

    @Override
    protected String getResourceType() {
        return "VPC";
    }

    @Override
    protected String toString(Vpc vpc) {
        return vpc.vpcId();
    }
}
