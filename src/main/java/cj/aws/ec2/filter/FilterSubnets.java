package cj.aws.ec2.filter;

import cj.aws.AWSFilter;
import cj.aws.AWSInput;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Subnet;

import javax.enterprise.context.Dependent;

import static cj.aws.AWSOutput.SubnetMatch;

@Dependent
public class FilterSubnets extends AWSFilter {


    private boolean match(Subnet net) {
        var vpcId = getInputString(AWSInput.targetVPCId);
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
    public void apply() {
        var ec2 = aws().ec2();
        var describeNets = DescribeSubnetsRequest.builder().build();
        var nets = ec2.describeSubnets(describeNets).subnets().stream();
        var matches = nets.filter(this::match).toList();
        success(SubnetMatch, matches);
    }
}
