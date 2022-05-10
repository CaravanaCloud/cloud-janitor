package cloudjanitor.aws.ec2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.stream.Stream;
@Dependent
public class FilterSubnets extends AWSFilter {
    String vpcId;

    public Task withVpcId(String vpcId) {
        this.vpcId = vpcId;
        return this;
    }


    private boolean match(Subnet net) {
        if (vpcId != null) {
            return net.vpcId().equals(vpcId);
        }else if (hasFilterPrefix()){
            var match = net.tags()
                    .stream()
                    .anyMatch(tag ->
                            tag.key().equals("Name") && matchName(tag.value()));
            return match;
        }else return true;
    }


    @Override
    public void runSafe() {
        var ec2 = aws().newEC2Client(getRegion());
        var describeNets = DescribeSubnetsRequest.builder().build();
        var nets = ec2.describeSubnets(describeNets).subnets().stream();
        var matches = nets.filter(this::match).toList();
        success("aws.subnet.matches",matches);
    }
}
