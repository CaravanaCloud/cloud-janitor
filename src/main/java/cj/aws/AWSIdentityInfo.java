package cj.aws;

public record AWSIdentityInfo(String userARN,
                              String accountId,
                              String accountAlias
) {
    public static AWSIdentityInfo of(String userARN, String accountId, String accountAlias) {
        return new AWSIdentityInfo(userARN, accountId, accountAlias);
    }
}
