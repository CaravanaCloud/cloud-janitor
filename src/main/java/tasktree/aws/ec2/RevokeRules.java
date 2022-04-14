package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.*;
import tasktree.aws.AWSDelete;

public class RevokeRules extends AWSDelete<SecurityGroup> {

    public RevokeRules(SecurityGroup resource) {
        super(resource);
    }

    @Override
    protected void cleanup(SecurityGroup resource) {
        deleteRules(resource);
    }

    private void deleteRules(SecurityGroup resource) {
        var groupId = resource.groupId();
        log().debug("Deleting Security Group Rules for [{}]", groupId);
        var describeRules = DescribeSecurityGroupRulesRequest
                .builder()
                .build();
        var ec2 = newEC2Client();
        var rules = ec2.describeSecurityGroupRules(describeRules).securityGroupRules();
        rules.forEach(rule -> {
            log().debug("Found security group rule [{}]",rule);
            if (rule.groupId().equals(resource.groupId())){
                if (rule.isEgress()){
                    log().trace("Deleting security group egress {}", rule);
                    var request = RevokeSecurityGroupEgressRequest
                            .builder()
                            .groupId(resource.groupId())
                            .securityGroupRuleIds(rule.securityGroupRuleId())
                            .build();
                    ec2.revokeSecurityGroupEgress(request);
                }else{
                    log().trace("Deleting security group ingress {}", rule);
                    var request = RevokeSecurityGroupIngressRequest
                            .builder()
                            .groupId(resource.groupId())
                            .securityGroupRuleIds(rule.securityGroupRuleId())
                            .build();
                    ec2.revokeSecurityGroupIngress(request);
                }
            }
        });

    }


    @Override
    protected String getResourceType() {
        return "Security Group Rules";
    }
}
