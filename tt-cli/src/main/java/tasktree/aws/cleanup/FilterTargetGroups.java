package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterTargetGroups extends AWSTask {
    static final Logger log = LoggerFactory.getLogger(FilterInstances.class);

    private boolean match(TargetGroup resource) {
        var prefix = getConfig().getAwsCleanupPrefix();
        var match = resource.targetGroupName().startsWith(prefix);
        log.info("Found Load Balancer {} {}", mark(match), resource);
        return match;
    }

    private List<TargetGroup> filterResources() {
        var elb = newELBClient();
        var resources = elb.describeTargetGroups().targetGroups();
        var matches = resources.stream().filter(this::match).toList();
        log.info("Matched {} Target Groups in region [{}]", matches.size(), getRegion());
        return matches;
    }


    @Override
    public void run() {
        var resources = filterResources();
        dryPush(deleteTasks(resources));
    }


    private Stream<Task> deleteTasks(List<TargetGroup> subnets) {
        return subnets.stream().map(this::deleteTask);
    }


    private Task deleteTask(TargetGroup resource) {
        return new DeleteTargetGroup(getConfig(), resource);
    }

}
