package cloudjanitor.aws;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

public class DefaultAWSIdentity
        extends AWSIdentity {
    private static final AWSIdentity instance = new DefaultAWSIdentity();

    public static AWSIdentity of() {
        return instance;
    }

    @Override
    public AwsCredentialsProvider toCredentialsProvider() {
        return software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
    }

    @Override
    public String toString() {
        return "Default AWS Identity";
    }
}
