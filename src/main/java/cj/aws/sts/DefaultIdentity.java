package cj.aws.sts;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;

public record DefaultIdentity() implements cj.aws.AWSIdentity  {
    private static final DefaultIdentity instance = new DefaultIdentity();

    public static DefaultIdentity of() {
        return instance;
    }

    @Override
    public AwsCredentialsProvider toCredentialsProvider(StsClient sts) {
        return software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
    }

    @Override
    public String toString() {
        return "DefaultIdentity{}";
    }
}
