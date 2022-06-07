package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterLoadBalancersV2 extends AWSFilter {

    private boolean match(LoadBalancer resource) {
        var lbName = resource.loadBalancerName();
        return matchName(lbName);
    }

    @Override
    public void runSafe() {
        var elb = aws().getELBClientV2();
        var resources = elb.describeLoadBalancers().loadBalancers();
        var matches = resources.stream().filter(this::match).toList();
        success(Output.AWS.ELBV2Match, matches);
    }

}
