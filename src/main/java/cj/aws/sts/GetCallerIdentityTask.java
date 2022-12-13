package cj.aws.sts;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.TaskRepeat;
import cj.TaskRepeater;
import cj.aws.AWSFilter;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.StsException;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import static cj.TaskMaturity.Level.experimental;
import static cj.aws.AWSOutput.CallerIdentity;

@Dependent
@Named("aws-get-caller-identity")
@TaskMaturity(experimental)
@TaskDescription("Get the caller identity from AWS")
@TaskRepeater(TaskRepeat.each_identity)
public class GetCallerIdentityTask extends AWSFilter {
    @Override
    public void apply() {
        try (var sts = aws().sts()){
            getCallerIdentity(sts);
        }catch (StsException ex){
            warn("Failed to get AWS caller identity");
        }
    }

    private void getCallerIdentity(StsClient sts) {
        var resp = sts.getCallerIdentity();
        var accountId = resp.account();
        var userARN = resp.arn();
        var userId = resp.userId();
        var accountAlias = lookupAccountAlias(accountId);

        var callerId = new SimpleIdentity(accountId,
                userARN,
                userId,
                accountAlias);

        debug("Got caller identity {}", callerId);
        success(CallerIdentity, callerId);
    }

    private String lookupAccountAlias(String accountId) {
        try(var iam = aws().iam()){
            return lookupAccountAlias(iam, accountId);
        }
    }

    private String lookupAccountAlias(IamClient iam, String accountId) {
        var aliases = iam.listAccountAliases().accountAliases();
        if (aliases.isEmpty()){
            return accountId;
        }else{
            var aliasesStr = String.join(",", aliases);
            log().debug("Found alias for account {}: {}", accountId, aliasesStr);
            return aliasesStr;
        }
    }

}
