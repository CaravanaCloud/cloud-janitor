package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public class FilterSecurityGroupRules extends AWSFilter {
    /*
    private String vpcId;

    public FilterSecurityGroupRules(String vpcId) {
        this.vpcId = vpcId;
    }

    private boolean match(IpPermission resource) {
        return true;
    }

    @Override
    protected List<SecurityGroup> filterResources() {
        var client = aws().newEC2Client(getRegion());
        var resources = client.describeSecurityGroups().securityGroups();
        var matches = resources.stream().filter(this::matchVPC);
        return matches.toList();
    }

    private boolean matchVPC(SecurityGroup securityGroup) {
        return vpcId.equals(securityGroup.vpcId());
    }

    @Override
    protected Stream<Task> mapSubtasks(SecurityGroup resource) {
        return Stream.of(new RevokeRules(resource));
    }

    @Override
    protected String getResourceType() {
        return "Security Group Rules";
    }
    */
}

