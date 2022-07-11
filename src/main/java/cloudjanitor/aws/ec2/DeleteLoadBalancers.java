package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cloudjanitor.Input.AWS.TargetLoadBalancerArn;
import static cloudjanitor.Output.AWS.LoadBalancerMatch;
@Dependent
public class DeleteLoadBalancers extends AWSTask {
    @Inject
    FilterLoadBalancers filterLoadBalancer;

    @Inject
    Instance<DeleteLoadBalancer> deleteLoadBalancerInstance;
    @Override
    public Task getDependency() {
        return delegate(filterLoadBalancer);
    }

    @Override
    public void apply() {
        var lbs = filterLoadBalancer.outputList(LoadBalancerMatch, LoadBalancer.class);
        lbs.stream().forEach(this::deleteLoadBalancer);
    }

    private void deleteLoadBalancer(LoadBalancer loadBalancer) {
        var delLbTask = deleteLoadBalancerInstance
                .get()
                .withInput(TargetLoadBalancerArn, loadBalancer.loadBalancerArn());
        submit(delLbTask);
    }
}
