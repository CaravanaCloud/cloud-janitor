package cj.aws.ec2.delete;

import cj.aws.AWSWrite;
import cj.aws.ec2.filter.FilterSecurityGroups;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesRequest;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static cj.aws.AWSOutput.*;
import static cj.Utils.msToStr;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;

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
        debug("Deleting {} security groups ", groups.size());
        groups.forEach(this::deleteGroup);
    }

    private void deleteGroup(SecurityGroup sg) {
        if (isDefault(sg)){
            warn("Can not delete default security group");
            return;
        }
        waitUntilEmpty(sg);
        var req = DeleteSecurityGroupRequest
                .builder()
                .groupId(sg.groupId())
                .build();
        aws().ec2().deleteSecurityGroup(req);
        debug("Deleted Security Group {}", sg);
    }

    private boolean isDefault(SecurityGroup sg) {
        return "default".equals(sg.groupName());
    }

    private void waitUntilEmpty(SecurityGroup sg) {
        var atMost = config().largeAtMostTimeoutMs();
        var pollInterval = config().mediumPollIntervalMs();
        debug("Waiting until sg is empty {} ({}|{}).", sg.groupId(), msToStr(pollInterval), msToStr(atMost));
        await().atMost(atMost, MILLISECONDS)
                    .pollInterval(pollInterval, MILLISECONDS)
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
        debug("Found {} enis for security group {}. {}", enis.size(), sg.groupId(), eniIds);
        return enis.isEmpty();
    }


}
