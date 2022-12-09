package cj.aws.ec2.filter;

import cj.aws.AWSFilter;
import cj.aws.AWSInput;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import javax.enterprise.context.Dependent;

import static cj.aws.AWSOutput.SecurityGroupsMatch;

@Dependent
public class FilterSecurityGroups extends AWSFilter {
    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var resources = ec2.describeSecurityGroups().securityGroups();
        var matches = resources.stream().filter(this::match).toList();
        success(SecurityGroupsMatch, matches);
    }

    private boolean match(SecurityGroup securityGroup) {
        var match = ! "default".equals(securityGroup.groupName());

        var vpcId = inputString(AWSInput.targetVPCId);
        if (vpcId.isPresent()){
            match = match && vpcId.get().equals(securityGroup.vpcId());
        }

        return match;
    }
}

