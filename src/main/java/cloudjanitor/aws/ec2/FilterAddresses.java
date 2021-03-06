package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DescribeAddressesRequest;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.stream.Stream;

import static cloudjanitor.Output.AWS.AddressMatch;

@Dependent
public class FilterAddresses extends AWSFilter {

    private boolean match(Address addr) {
        var prefix = aws().config().filterPrefix();
        var match = true;
        if (prefix.isPresent()) {
            match = addr.tags().stream()
                    .anyMatch(tag -> tag.key().equals("Name") && tag.value().startsWith(prefix.get()));
        }
        return match;
    }

    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var describeAddresses = DescribeAddressesRequest.builder().build();
        var addresses = ec2.describeAddresses(describeAddresses).addresses().stream();
        var matches = addresses.filter(this::match).toList();
        success(AddressMatch, matches);
    }
}
