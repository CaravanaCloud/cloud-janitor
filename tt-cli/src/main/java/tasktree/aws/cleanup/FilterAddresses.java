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

    private boolean match(Address addr) {
        var prefix = getAwsCleanupPrefix();
        var match = addr.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix));
        return match;
    }

    @Override
    protected List<Address> filterResources() {
        var ec2 = newEC2Client();
        var describeAddresses = DescribeAddressesRequest.builder().build();
        var addresses = ec2.describeAddresses(describeAddresses).addresses().stream();
        var matches = addresses.filter(this::match).toList();
        return matches;
    }


    @Override
    protected Stream<Task> mapSubtasks(Address addr) {
        return Stream.of(new ReleaseAddress(addr));
    }

    @Override
    protected String toString(Address address) {
        return address.publicIp();
    }

    @Override
    protected String getResourceType() {
        return "Elastic IPs";
    }
}
