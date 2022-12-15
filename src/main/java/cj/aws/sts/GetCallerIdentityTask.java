package cj.aws.sts;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.TaskRepeat;
import cj.TaskRepeater;
import cj.aws.AWSClientsManager;
import cj.aws.AWSFilter;
import cj.aws.AWSIdentity;
import cj.aws.AWSIdentityInfo;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.sts.StsClient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.TaskMaturity.Level.experimental;

@Dependent
@Named("aws-get-caller-identity")
@TaskMaturity(experimental)
@TaskDescription("Get the caller identity from AWS")
@TaskRepeater(TaskRepeat.each_identity)
public class GetCallerIdentityTask extends AWSFilter {
    @Inject
    AWSClientsManager awsManager;


    @Override
    public void applyIdentity(AWSIdentity identity) {
        log().trace("Looking up caller identity for {}", identity);
        try (var sts = aws().sts()){
            var info = getCallerIdentity(sts, identity);
            log().debug("Found caller identity {}", info);
            success(info);
        }catch (Exception ex){
            ex.printStackTrace();
            throw fail("Failed to get AWS caller identity for {}", identity);
        }
    }

    private AWSIdentityInfo getCallerIdentity(StsClient sts, AWSIdentity id) {
        var resp = sts.getCallerIdentity();
        var accountId = resp.account();
        var userARN = resp.arn();
        var userId = resp.userId();
        var accountAlias = lookupAccountAlias(accountId);
        var info = AWSIdentityInfo.of(accountId, accountAlias, userARN);
        awsManager.putInfo(id, info);
        trace("Got caller identity [{}] [{}] [{}]",
                accountId, accountAlias, userARN);
        return info;
    }

    private String lookupAccountAlias(String accountId) {
        try(var iam = aws().iam()){
            return lookupAccountAlias(iam, accountId);
        }catch (Exception ex) {
            error("Failed to lookup account alias", ex);
            return accountId;
        }
    }

    private String lookupAccountAlias(IamClient iam, String accountId) {
        var aliases = iam.listAccountAliases().accountAliases();
        if (aliases.isEmpty()){
            return accountId;
        }else{
            var aliasesStr = String.join(",", aliases);
            log().trace("Found alias for account {}: {}", accountId, aliasesStr);
            return aliasesStr;
        }
    }

}
