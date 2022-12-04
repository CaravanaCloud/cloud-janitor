package cj.aws;

import software.amazon.awssdk.regions.Region;

public record AWSClientIdentity(AWSIdentity identity,
                                Region region) {
    public static AWSClientIdentity of(AWSIdentity identity, Region region) {
        return new AWSClientIdentity(identity, region);
    }
}
