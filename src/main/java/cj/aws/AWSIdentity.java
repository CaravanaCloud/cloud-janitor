package cj.aws;

import cj.aws.sts.CallerIdentity;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

public abstract class AWSIdentity {

    private CallerIdentity callerId = null;

    public abstract AwsCredentialsProvider toCredentialsProvider();

    public AWSIdentity withCallerIdentity(CallerIdentity callerId) {
        this.callerId = callerId;
        return this;
    }

    public boolean hasCallerIdentity(){
        return callerId != null;
    }

    public String getAccountName() {
        return callerId == null ? "" : callerId.getAccountName();
    }

    @Override
    public String toString() {
        return getAccountName();
    }

    public void setCallerIdentity(CallerIdentity callerIdentity) {
        this.callerId = callerIdentity;
    }
}
