package cloudjanitor.aws;

import cloudjanitor.aws.sts.CallerIdentity;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Optional;

public abstract class AWSIdentity {

    private Optional<CallerIdentity> callerId = Optional.empty();

    public abstract AwsCredentialsProvider toCredentialsProvider();

    public AWSIdentity withCallerIdentity(Optional<CallerIdentity> callerId) {
        this.callerId = callerId;
        return this;
    }

    public boolean hasCallerIdentity(){
        return callerId.isPresent();
    }

    public String getAccountName() {
        var name = callerId.map(CallerIdentity::getAccountName).orElse(" ");
        return name;
    }

}
