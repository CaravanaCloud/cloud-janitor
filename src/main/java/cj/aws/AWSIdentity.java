package cj.aws;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

public interface AWSIdentity {

    AwsCredentialsProvider toCredentialsProvider();


    String accountId();
    String accountAlias();
}
