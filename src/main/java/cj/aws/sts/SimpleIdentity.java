package cj.aws.sts;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

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
    public AwsCredentialsProvider toCredentialsProvider() {
        return software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
    }
}
