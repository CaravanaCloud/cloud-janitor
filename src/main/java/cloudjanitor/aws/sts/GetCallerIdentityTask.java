package cloudjanitor.aws.sts;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSFilter;

import javax.enterprise.context.Dependent;
import java.util.Optional;

@Dependent
public class GetCallerIdentityTask extends AWSFilter {
    @Override
    public void apply() {
        var accountId = lookupAccountId();
        var accountAlias = lookupAccountAlias(accountId);
        var callerId = new CallerIdentity(accountId, accountAlias);
        trace("GetCallerIdentity {}", callerId);
        success(Output.AWS.CallerIdentity, callerId);
    }

    private Optional<String> lookupAccountAlias(String accountId) {
        var iam = aws().iam();
        var aliases = iam.listAccountAliases().accountAliases();
        if (aliases.isEmpty()){
            return Optional.empty();
        }else{
            var aliasesStr = String.join(",", aliases);
            info("Found alias for account {}: {}", accountId, aliasesStr);
            return Optional.of(aliasesStr);
        }
    }

    private String lookupAccountId() {
        var sts = aws().sts();
        var resp = sts.getCallerIdentity();
        var accountId = resp.account();
        return accountId;
    }
}
