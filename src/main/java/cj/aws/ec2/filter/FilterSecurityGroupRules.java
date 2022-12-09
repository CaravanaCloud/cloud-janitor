package cj.aws.ec2.filter;

import cj.aws.AWSFilter;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupRulesRequest;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.SecurityGroupRule;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;

import static cj.aws.AWSInput.targetVPCId;
import static cj.aws.AWSOutput.SecurityGroupRulesMatch;

@Dependent
public class FilterSecurityGroupRules extends AWSFilter {

    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var resources = ec2.describeSecurityGroups().securityGroups();
        var matches = resources.stream().filter(this::matchVPC).toList();
        var rules = new ArrayList<SecurityGroupRule>();
        for (SecurityGroup sg: matches) {
            var req = DescribeSecurityGroupRulesRequest
                    .builder()
                    .filters(filter("group-id", sg.groupId()))
                    .build();
            var newRules = ec2.describeSecurityGroupRules(req).securityGroupRules();
            rules.addAll(newRules);
        }
        debug("Matched {} security group rules ", rules.size());
        success(SecurityGroupRulesMatch, rules);
    }

    private boolean matchVPC(SecurityGroup securityGroup) {
        var match = true;
        var vpcId = inputString(targetVPCId);
        if (vpcId.isPresent()){
            match = match && vpcId.get().equals(securityGroup.vpcId());
        }
        return match;
    }
}

