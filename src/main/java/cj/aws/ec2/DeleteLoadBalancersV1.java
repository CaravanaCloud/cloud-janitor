package cj.aws.ec2;

import cj.Output;
import cj.aws.AWSTask;
import cj.spi.Task;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.Input.AWS.targetLoadBalancerName;

@Dependent
public class DeleteLoadBalancersV1 extends AWSTask {
    @Inject
    FilterLoadBalancersV1 filterLoadBalancer;

    @Inject
    Instance<DeleteLoadBalancerV1> deleteLoadBalancerInstance;

    @Override
    public Task getDependency() {
        return delegate(filterLoadBalancer);
    }

    @Override
    public void apply() {
        var lbs = filterLoadBalancer.outputList(Output.AWS.LBDescriptionMatch, LoadBalancerDescription.class);
        lbs.stream().forEach(this::deleteLoadBalancer);
    }

    private void deleteLoadBalancer(LoadBalancerDescription loadBalancer) {
        var delLbTask = deleteLoadBalancerInstance
                .get()
                .withInput(targetLoadBalancerName, loadBalancer.loadBalancerName());
        submit(delLbTask);
    }
}