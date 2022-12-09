package cj.aws.sts;

import cj.BaseTask;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.sts.StsClient;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import static cj.aws.AWSOutput.CallerIdentity;

@Dependent
@Named("aws-get-default-identity")
public class GetDefaultAWSIdentity extends BaseTask {
    @Override
    public void apply() {
        try(var sts = StsClient.builder().build()){
            var callerId = getCallerIdentity(sts);
            if (callerId != null){
                trace("Found default AWS identity: {}", callerId);
                success(CallerIdentity, callerId);
            }else {
                warn("Failed to load default aws identity.");
            }
        }
    }

    private SimpleIdentity getCallerIdentity(StsClient sts) {
        var resp = sts.getCallerIdentity();
        var accountId = resp.account();
        var userARN = resp.arn();
        var userId = resp.userId();
        var accountAlias = lookupAccountAlias(accountId);

        @SuppressWarnings("VariableTypeCanBeExplicit")
        var callerId = new SimpleIdentity(accountId,
                userARN,
                userId,
                accountAlias);

        return callerId;
    }

    private String lookupAccountAlias(String accountId) {
        try(var iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build()){
            return lookupAccountAlias(iam, accountId);
        }
    }

    private String lookupAccountAlias(IamClient iam, String accountId) {
        var aliases = iam.listAccountAliases().accountAliases();
        if (aliases.isEmpty()){
            trace("Alias not found for account {}.", accountId);
            return accountId;
        }else{
            var aliasesStr = String.join(",", aliases);
            trace("Alias found for account {}: {}", accountId, aliasesStr);
            return aliasesStr;
        }
    }





}
