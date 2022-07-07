package cloudjanitor.aws.ec2;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSCleanup;
import cloudjanitor.aws.AWSWrite;
import cloudjanitor.aws.model.IpPermissionModel;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.RevokeSecurityGroupEgressRequest;
import software.amazon.awssdk.services.ec2.model.RevokeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.SecurityGroupRule;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class DeleteSecurityGroupRules extends AWSWrite {
    @Inject
    FilterSecurityGroupRules filterRules;

    @Override
    public Task getDependency() {
        return filterRules;
    }

    @Override
    public void apply() {
        var rules = filterRules.outputList(Output.AWS.SecurityGroupRulesMatch, SecurityGroupRule.class);
        rules.forEach(this::deleteRule);
    }

    private void deleteRule(SecurityGroupRule sgr) {
        if (sgr.isEgress()){
           var req = RevokeSecurityGroupEgressRequest.builder().groupId(sgr.groupId())
                    .securityGroupRuleIds(sgr.securityGroupRuleId()).build();
           aws().ec2().revokeSecurityGroupEgress(req);
           log().debug("Deleted security group rule egress {}/{}", sgr.groupId(), sgr.securityGroupRuleId());
        }else {
            var req = RevokeSecurityGroupIngressRequest.builder().groupId(sgr.groupId())
                    .securityGroupRuleIds(sgr.securityGroupRuleId()).build();
            aws().ec2().revokeSecurityGroupIngress(req);
            log().debug("Deleted security group rule ingress {}/{}", sgr.groupId(), sgr.securityGroupRuleId());
        }
    }

}
