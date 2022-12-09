package cj.aws.ec2.delete;

import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterLoadBalancersV1;
import cj.spi.Task;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.aws.AWSInput.targetLoadBalancerName;
import static cj.aws.AWSOutput.LBDescriptionMatch;
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
        var lbs = filterLoadBalancer.outputList(LBDescriptionMatch, LoadBalancerDescription.class);
        lbs.stream().forEach(this::deleteLoadBalancer);
    }

    private void deleteLoadBalancer(LoadBalancerDescription loadBalancer) {
        var delLbTask = deleteLoadBalancerInstance
                .get()
                .withInput(targetLoadBalancerName, loadBalancer.loadBalancerName());
        submit(delLbTask);
    }
}
