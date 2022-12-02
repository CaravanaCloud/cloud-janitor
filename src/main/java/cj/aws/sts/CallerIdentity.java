package cj.aws.sts;

public record CallerIdentity(
        String accountId,
        String userARN,
        String userId,
        String accountAlias) {
    public String getAccountName(){
        if (accountAlias != null)
            return accountAlias;
        return accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
