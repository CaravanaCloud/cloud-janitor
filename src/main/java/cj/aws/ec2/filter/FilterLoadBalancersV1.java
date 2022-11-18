package cj.aws.ec2.filter;

import cj.aws.AWSFilter;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;

import javax.enterprise.context.Dependent;

import static cj.Output.aws.LBDescriptionMatch;
import static cj.aws.AWSInput.targetVPCId;

@Dependent
public class FilterLoadBalancersV1 extends AWSFilter {

    private boolean match(LoadBalancerDescription resource) {
        var vpcId = inputString(targetVPCId);
        var match = true;
        if (vpcId.isPresent()) {
            match = resource.vpcId().equals(vpcId.get());
        }
        return match;
    }

    @Override
    public void apply() {
        var elb = aws().elbv1();
        var resources = elb.describeLoadBalancers().loadBalancerDescriptions();
        var matches = resources.stream().filter(this::match).toList();
        debug("Matched {}/{} classic load balancers (v1)",  matches.size(), resources.size());
        success(LBDescriptionMatch, matches);
    }

}
