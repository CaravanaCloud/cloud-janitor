package cj.aws.sts;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;

public record SimpleIdentity(
        String accountId,
        String userARN,
        String userId,
        String accountAlias) implements cj.aws.AWSIdentity  {
    public String getAccountName(){
        if (accountAlias != null)
            return accountAlias;
        return accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    @Override
    public AwsCredentialsProvider toCredentialsProvider(StsClient sts) {
        return software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
    }
}
