package tasktree.aws.ec2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.aws.AWSFilter;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.stream.Stream;

@Dependent
public class FilterVPCs extends AWSFilter<Vpc> {
    static final Logger log = LoggerFactory.getLogger(FilterVPCs.class);

    private boolean matchPrefix(Vpc vpc) {
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
        var matches = findAll().stream().toList();
        if (getAwsCleanupPrefix() != null && ! getAwsCleanupPrefix().isEmpty())
            matches = matches.stream().filter(this::matchPrefix).toList();
        if (get("vpc.id") != null)
            matches = matches.stream().filter(this::matchVpcId).toList();
        return matches;
    }

    private boolean matchVpcId(Vpc vpc) {
        var vpcId = get("vpc.id");
        var match = vpc.vpcId().equals(vpcId);
        return match;
    }

    @Override
    public Stream<Task> mapSubtasks(Vpc vpc) {
        var vpcId = vpc.vpcId();
        return Stream.of(
                new DeleteVpc(vpcId)
        );
    }

    @Override
    protected String getResourceType() {
        return "VPC";
    }

}
