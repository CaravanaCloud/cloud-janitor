package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import cloudjanitor.aws.AWSFilter;

import javax.enterprise.context.Dependent;

@Dependent
public class FilterSecurityGroups extends AWSFilter {
    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var resources = ec2.describeSecurityGroups().securityGroups();
        var matches = resources.stream().filter(this::match).toList();
        success(Output.AWS.SecurityGroupsMatch, matches);
    }

    private boolean match(SecurityGroup securityGroup) {
        var match = ! "default".equals(securityGroup.groupName());

        var vpcId = inputString(Input.AWS.TargetVpcId);
        if (vpcId.isPresent()){
            match = match && vpcId.get().equals(securityGroup.vpcId());
        }

        return match;
    }
}

