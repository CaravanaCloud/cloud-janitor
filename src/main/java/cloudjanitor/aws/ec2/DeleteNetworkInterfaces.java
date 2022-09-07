package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cloudjanitor.Input.AWS.targetLoadBalancerArn;
import static cloudjanitor.Output.AWS.ELBV2Match;

@Dependent
public class DeleteNetworkInterfaces extends AWSTask {
    @Inject
    FilterNetworkInterfaces filterENIs;

    @Inject
    Instance<DeleteNetworkInterface> deleteENIInstance;

    @Override
    public Task getDependency() {
        return delegate(filterENIs);
    }

    @Override
    public void apply() {
        var lbs = filterENIs.outputList(Output.AWS.NetworkINterfacesMatch, NetworkInterface.class);
        lbs.stream().forEach(this::deleteNetworkInterface);
    }

    private void deleteNetworkInterface(NetworkInterface eni) {
        var delLbTask = deleteENIInstance
                .get()
                .withInput(Input.AWS.targetNetworkInterfaceId, eni.networkInterfaceId());
        submit(delLbTask);
    }
}
