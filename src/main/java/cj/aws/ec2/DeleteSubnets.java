package cj.aws.ec2;

import cj.Output;
import cj.aws.AWSWrite;
import cj.spi.Task;
import org.awaitility.core.ConditionTimeoutException;
import software.amazon.awssdk.services.ec2.model.*;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static software.amazon.awssdk.services.ec2.model.InstanceStateName.RUNNING;
import static software.amazon.awssdk.services.ec2.model.InstanceStateName.SHUTTING_DOWN;
import static org.awaitility.Awaitility.*;

@Dependent
public class DeleteSubnets extends AWSWrite {
    @Inject
    FilterSubnets filterSubnets;

    @Inject
    Instance<DeleteSubnet> deleteSubnet;

    @Override
    public Task getDependency() {
        return delegate(filterSubnets);
    }

    @Override
    public void apply() {
        var subnets = outputList(Output.AWS.SubnetMatch, Subnet.class);
        subnets.forEach(this::deleteSubnet);
    }

    private void deleteSubnet(Subnet subnet) {
        try {
            waitUntilEmpty(subnet);
            var delSubnet = create(deleteSubnet).withSubnet(subnet);
            submit(delSubnet);
        }catch (ConditionTimeoutException ex) {
            fail("Failed to empty subnet for cleanup in time." + subnet.subnetId());
        }
    }

    private void waitUntilEmpty(Subnet subnet) {
        await().atMost(10, MINUTES)
                .pollInterval(15, SECONDS)
                .until(() -> isEmpty(subnet));
    }

    private boolean isEmpty(Subnet subnet) {
        var req = DescribeInstancesRequest
                .builder()
                .filters(filter("subnet-id", subnet.subnetId()))
                .build();
        var instances = aws().ec2().describeInstances(req).reservations()
                .stream()
                .flatMap( r -> r.instances().stream());
        var activeInstances = instances
                .map(i -> i.state().name())
                .filter( state -> RUNNING.equals(state) || SHUTTING_DOWN.equals(state))
                .findAny()
                .isPresent();
        debug("Active instances in subnet {}? {}", subnet.subnetId(), activeInstances);
        return ! activeInstances;
    }
}
