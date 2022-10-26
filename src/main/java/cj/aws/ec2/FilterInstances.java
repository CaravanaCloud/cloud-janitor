package cj.aws.ec2;

import cj.Input;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import cj.aws.AWSFilter;

import javax.enterprise.context.Dependent;

import static cj.Output.AWS.InstancesMatch;


@Dependent
public class FilterInstances extends AWSFilter {

    private boolean match(Instance instance) {
        var match = true;

        var state = instance.state().nameAsString().toLowerCase();
        var terminated = state.equals("terminated");

        if (terminated) return false;

        var vpcId = inputString(Input.AWS.targetVPCId);
        if (vpcId.isPresent()){
            var vpcMatch = vpcId.get().equals(instance.vpcId());
            match = match && vpcMatch;
        }

        var prefix = aws().config().filterPrefix();
        if (prefix.isPresent()) {
            var prefixMatch = instance.tags().stream()
                    .anyMatch(tag -> tag.key().equals("Name")
                            && tag.value().startsWith(prefix.get()));
            match = match && prefixMatch;
        }
        return match;
    }

    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var describeInstances = DescribeInstancesRequest.builder().build();
        var reservations = ec2.describeInstancesPaginator(describeInstances).reservations().stream();
        var instances = reservations.flatMap(reservation -> reservation.instances().stream()).toList();
        var matches = instances.stream().filter(this::match).toList();
        debug("Matched {}/{} instances",  matches.size(), instances.size());
        success(InstancesMatch, matches);
    }
}
