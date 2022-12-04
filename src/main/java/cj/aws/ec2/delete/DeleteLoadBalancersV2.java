package cj.aws.ec2.delete;

import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterLoadBalancersV2;
import cj.spi.Task;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.aws.AWSOutput.*;
import static cj.aws.AWSInput.targetLoadBalancerArn;

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
