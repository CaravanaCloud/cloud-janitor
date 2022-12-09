package cj.aws.ec2.delete;

import cj.aws.AWSInput;
import cj.aws.AWSTask;
import cj.aws.ec2.filter.FilterSecurityGroupRules;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.SecurityGroupRule;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.aws.AWSOutput.SecurityGroupRulesMatch;

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
        var rules = filterRules.outputList(SecurityGroupRulesMatch, SecurityGroupRule.class);
        rules.forEach(this::deleteRule);
    }

    private void deleteRule(SecurityGroupRule sgr) {
        var deleteRule = deleteRuleInstance.get()
                .withInput(AWSInput.securityGroupRule, sgr);
        submit(deleteRule);
    }

}
