package cj.aws.ec2.filter;

import cj.aws.AWSFilter;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import javax.enterprise.context.Dependent;

import static cj.aws.AWSOutput.ELBV2Match;

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
        success(ELBV2Match, matches);
    }

}
