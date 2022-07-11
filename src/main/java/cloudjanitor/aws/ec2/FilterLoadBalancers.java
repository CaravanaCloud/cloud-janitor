package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import cloudjanitor.aws.AWSFilter;

import javax.enterprise.context.Dependent;

import static cloudjanitor.Input.AWS.TargetVpcId;
import static cloudjanitor.Output.AWS.LBDescriptionMatch;

@Dependent
public class FilterLoadBalancers extends AWSFilter {

    private boolean match(LoadBalancerDescription resource) {
        var vpcId = inputString(TargetVpcId);
        var match = true;
        if (vpcId.isPresent()) {
            match = resource.vpcId().equals(vpcId.get());
        }
        return match;
    }

    @Override
    public void apply() {
        var elb = aws().elb();
        var resources = elb.describeLoadBalancers().loadBalancerDescriptions();
        var matches = resources.stream().filter(this::match).toList();
        success(LBDescriptionMatch, matches);
    }

}
