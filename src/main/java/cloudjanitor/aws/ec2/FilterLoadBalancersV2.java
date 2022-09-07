package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSFilter;

import javax.enterprise.context.Dependent;

@Dependent
public class FilterLoadBalancersV2 extends AWSFilter {

    private boolean match(LoadBalancer resource) {
        var lbName = resource.loadBalancerName();
        return matchName(lbName);
    }

    @Override
    public void apply() {
        var elb = aws().elbv2();
        var resources = elb.describeLoadBalancers().loadBalancers();
        var matches = resources.stream().filter(this::match).toList();
        debug("Matched {}/{} load balancers (v2)",  matches.size(), resources.size());
        success(Output.AWS.ELBV2Match, matches);
    }

}
