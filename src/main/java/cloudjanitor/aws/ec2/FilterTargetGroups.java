package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.stream.Stream;

import static cloudjanitor.Output.AWS.TargetGroupsMatch;

@Dependent
public class FilterTargetGroups extends AWSFilter {
    private boolean match(TargetGroup resource) {
        var prefix = aws().config().filterPrefix();
        var match = true;
        if (prefix.isPresent()) {
            match = resource.targetGroupName().startsWith(prefix.get());
        }
        return match;
    }

    @Override
    public void apply() {
        var elb = aws().elbv2();
        var resources = elb.describeTargetGroups().targetGroups();
        var matches = resources.stream().filter(this::match).toList();
        success(TargetGroupsMatch, matches);
    }

}
