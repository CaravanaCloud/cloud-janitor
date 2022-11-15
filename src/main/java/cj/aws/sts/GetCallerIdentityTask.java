package cj.aws.sts;

import cj.Output;
import cj.Tasks;
import cj.aws.AWSFilter;
import software.amazon.awssdk.services.sts.model.StsException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@Dependent
@Named("aws-get-caller-identity")
public class GetCallerIdentityTask extends AWSFilter {
    @Inject
    Tasks tasks;

    @Override
    public void apply() {
        try {
            var accountId = lookupAccountId();
            var accountAlias = lookupAccountAlias(accountId);
            var callerId = new CallerIdentity(accountId, accountAlias);
            info("GetCallerIdentity {}", callerId);
            success(Output.aws.CallerIdentity, callerId);
        }catch (StsException ex){
            warn("Failed to get AWS caller identity");
        }
    }

    private Optional<String> lookupAccountAlias(String accountId) {
        var iam = aws().iam();
        var aliases = iam.listAccountAliases().accountAliases();
        if (aliases.isEmpty()){
            return Optional.empty();
        }else{
            var aliasesStr = String.join(",", aliases);
            logger().info("Found alias for account {}: {}", accountId, aliasesStr);
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
