package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import java.util.List;

import static cloudjanitor.Output.AWS.SecurityGroupsMatch;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.*;

@Dependent
public class DeleteSecurityGroups extends AWSWrite {
    @Inject
    FilterSecurityGroups filterGroups;


    @Override
    public Task getDependency() {
        return filterGroups;
    }

    @Override
    public void apply() {
        var groups = filterGroups.outputList(SecurityGroupsMatch, SecurityGroup.class);
        log().debug("Deleting {} security groups", groups.size());
        groups.forEach(this::deleteGroup);
    }

    private void deleteGroup(SecurityGroup sg) {
        if (isDefault(sg)){
            log().warn("Can not delete default security group");
            return;
        }
        waitUntilEmpty(sg);
        var req = DeleteSecurityGroupRequest
                .builder()
                .groupId(sg.groupId())
                .build();
        aws().ec2().deleteSecurityGroup(req);
        log().debug("Deleted Security Group {}", sg);
    }

    private boolean isDefault(SecurityGroup sg) {
        return "default".equals(sg.groupName());
    }

    private void waitUntilEmpty(SecurityGroup sg) {
        log().debug("Waiting until sg is empty {}", sg.groupId());
        await().atMost(10, MINUTES)
                    .pollInterval(15, SECONDS)
                    .until(() -> isEmpty(sg));

    }

    private Boolean isEmpty(SecurityGroup sg) {
        var filterName = "group-id";
        var filterValue = sg.groupId();
        var req = DescribeNetworkInterfacesRequest.builder()
                .filters(filter(filterName, filterValue))
                .build();
        var enis = aws().ec2().describeNetworkInterfaces(req).networkInterfaces();
        var eniIds = enis.stream().map(eni -> eni.networkInterfaceId()).toList();
        log().debug("Found {} enis for security group {}. {}", enis.size(), sg.groupId(), eniIds);
        return enis.isEmpty();
    }


}
