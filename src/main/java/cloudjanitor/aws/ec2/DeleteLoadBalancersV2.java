package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cloudjanitor.Input.AWS.targetLoadBalancerArn;
import static cloudjanitor.Output.AWS.ELBV2Match;

@Dependent
public class DeleteLoadBalancersV2 extends AWSTask {
    @Inject
    FilterLoadBalancersV2 filterLoadBalancer;

    @Inject
    Instance<DeleteLoadBalancerV2> deleteLoadBalancerInstance;
    @Override
    public Task getDependency() {
        return delegate(filterLoadBalancer);
    }

    @Override
    public void apply() {
        var lbs = filterLoadBalancer.outputList(ELBV2Match, LoadBalancer.class);
        lbs.stream().forEach(this::deleteLoadBalancer);
    }

    private void deleteLoadBalancer(LoadBalancer loadBalancer) {
        var delLbTask = deleteLoadBalancerInstance
                .get()
                .withInput(targetLoadBalancerArn, loadBalancer.loadBalancerArn());
        submit(delLbTask);
    }
}
