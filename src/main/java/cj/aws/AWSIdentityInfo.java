package cj.aws;

public record AWSIdentityInfo(String accountId,
                              String accountAlias,
                              String userARN) {
    public static AWSIdentityInfo of(String accountId, String accountAlias, String userARN) {
        return new AWSIdentityInfo(accountId, accountAlias, userARN);
    }
}
