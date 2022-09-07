package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.RevokeSecurityGroupEgressRequest;
import software.amazon.awssdk.services.ec2.model.RevokeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.SecurityGroupRule;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Dependent
public class DeleteSecurityGroupRules extends AWSTask {
    @Inject
    FilterSecurityGroupRules filterRules;

    @Inject
    Instance<DeleteSecurityGroupRuleTask> deleteRuleInstance;
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
        var deleteRule = deleteRuleInstance.get()
                .withInput(Input.AWS.securityGroupRule, sgr);
        submit(deleteRule);
    }

}
