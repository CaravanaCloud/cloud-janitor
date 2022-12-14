package cj.aws;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;

public interface AWSIdentity {
    AwsCredentialsProvider toCredentialsProvider(StsClient sts);
}
