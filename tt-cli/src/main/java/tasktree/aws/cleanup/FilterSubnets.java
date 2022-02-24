package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterSubnets extends AWSFilter<Subnet> {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);
    private String vpcId;

    public FilterSubnets(Configuration config, String vpcId) {
        super(config);
        this.vpcId = vpcId;
    }

    private boolean match(Subnet net) {
        var prefix = getConfig().getAwsCleanupPrefix();
        var match = net.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        log.info("Found Subnet {} {}", mark(match), net);
        return match;
    }

    private List<Subnet> filterSubnets() {
        var ec2 = newEC2Client();
        var describeNets = DescribeSubnetsRequest.builder().build();
        var nets = ec2.describeSubnets(describeNets).subnets().stream();
        var matches = nets.filter(this::match).toList();
        log.info("Matched {} subnets in region [{}]", matches.size(), getRegion());
        return matches;
    }


    @Override
    public void run() {
        var subnets = filterSubnets();

        addAllTasks(deleteSubnets(subnets));
    }

    private Stream<Task> deleteSubnets(List<Subnet> subnets) {
        return subnets.stream().map(this::deleteSubnet);
    }


    private Task deleteSubnet(Subnet net) {
        return new DeleteSubnet(getConfig(), net);
    }
}
