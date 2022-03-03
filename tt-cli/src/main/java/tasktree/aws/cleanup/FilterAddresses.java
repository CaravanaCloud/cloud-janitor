package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesRequest;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class    FilterAddresses extends AWSFilter<Address> {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);

    private boolean match(Address addr) {
        var prefix = getAwsCleanupPrefix();
        var match = addr.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        return match;
    }

    private List<Address> filterEIPs() {
        var ec2 = newEC2Client();
        var describeAddresses = DescribeAddressesRequest.builder().build();
        var addresses = ec2.describeAddresses(describeAddresses).addresses().stream();
        var matches = addresses.filter(this::match).toList();
        log.info("Matched {} instances in region [{}]", matches.size(), getRegion());
        return matches;
    }

    private Task toTask(Address addr) {
        return new ReleaseAddress(getConfig(), addr);
    }

    @Override
    public void run() {
        var addrs = filterEIPs();
        addAllTasks(releaseAddresses(addrs));
    }

    private Stream<Task> releaseAddresses(List<Address> addrs) {
        return addrs.stream().map(this::toTask);
    }
}
