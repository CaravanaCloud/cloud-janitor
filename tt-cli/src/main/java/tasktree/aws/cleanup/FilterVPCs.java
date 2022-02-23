package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.Configuration;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterVPCs extends AWSFilter<Vpc> {
    static final Logger log = LoggerFactory.getLogger(FilterVPCs.class);

    public FilterVPCs(){}

    private boolean match(Vpc vpc) {
        var prefix = getConfig().getAwsCleanupPrefix();
        var match = vpc.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        log.info("Found Vpc {} {}", mark(match), vpc);
        return match;
    }

    private List<Vpc> filterVpcs() {
        var client = newEC2Client();
        var request = DescribeVpcsRequest.builder().build();
        var resources = client.describeVpcs(request).vpcs().stream();
        var matches = resources.filter(this::match).toList();
        log.info("Matched {} VPCs in region [{}]", matches.size(), getRegion());
        return matches;
    }


    @Override
    public void run() {
        var vpcs = filterVpcs();
        vpcs.stream().forEach(this::filterResources);
        dryPush(deleteVpcs(vpcs));
    }

    private void filterResources(Vpc vpc) {
        var vpcId = vpc.vpcId();
        addAllTasks(
                new FilterNetworkInterfaces(config, vpcId),
                new FilterSubnets(config, vpcId),
                new FilterRouteTables(config, vpcId),
                new FilterVPCEndpoints(config, vpcId),
                new FilterSecurityGroups(config, vpcId),
                new FilterInternetGateways(config, vpc.vpcId())
        );
    }

    private Stream<Task> deleteVpcs(List<Vpc> vpcs) {
        return vpcs.stream().map(this::deleteVpc);
    }


    private Task deleteVpc(Vpc resource) {
        return new DeleteVpc(getConfig(), resource);
    }
}
