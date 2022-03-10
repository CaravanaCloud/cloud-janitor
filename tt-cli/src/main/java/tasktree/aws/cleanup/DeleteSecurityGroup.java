package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.*;
import tasktree.Configuration;

public class DeleteSecurityGroup extends AWSDelete {
    private final SecurityGroup resource;

    public DeleteSecurityGroup(SecurityGroup resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        deleteRules();
        deleteSecurityGroup();
    }

    private void deleteRules() {
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
                    log().debug("Deleting security group egress {}", rule);
                    var request = RevokeSecurityGroupEgressRequest
                            .builder()
                            .groupId(resource.groupId())
                            .securityGroupRuleIds(rule.securityGroupRuleId())
                            .build();
                    ec2.revokeSecurityGroupEgress(request);
                }else{
                    log().debug("Deleting security group ingress {}", rule);
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


    private void deleteSecurityGroup() {
        log().debug("Deleting Security Group {}", resource.groupId());
        var request = DeleteSecurityGroupRequest.builder()
                .groupId(resource.groupId())
                .build();
        newEC2Client().deleteSecurityGroup(request);
    }

    @Override
    public String toString() {
        return super.toString("Security Group",
                resource.groupId());
    }
}
