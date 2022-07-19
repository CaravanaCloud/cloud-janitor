package cloudjanitor.aws.s3;

import cloudjanitor.Errors;
import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSWrite;
import cloudjanitor.aws.sts.GetCallerIdentityTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.s3.model.Bucket;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Optional;

@Dependent
public class GetDataBucket extends AWSWrite {
    @Inject
    GetCallerIdentityTask getCallerId;

    @Inject
    GetBucketTask getBucket;

    @Inject
    CreateBucketTask createBucket;

    @Override
    public Task getDependency() {
        return getCallerId;
    }

    @Override
    public void apply() {
        var bucketName = getDataBucketName();
        Optional<Bucket> bucket = getBucket(bucketName);
        if (bucket.isPresent()){
            log().debug("Found data bucket {}", bucket.get().name());
            success(Output.AWS.Bucket, bucket);
        }else {
            log().debug("Data bucket not found {}. Creating...", bucketName);
            submit(createBucket.withInput(Input.AWS.TargetBucketName, bucketName));
            log().debug("Checking if bucket was created");
            bucket = getBucket(bucketName);
            if (bucket.isPresent()){
                success(Output.AWS.Bucket, bucket);
            }else {
                error("Failed to create data bucket");
            }
        }

    }

    private Optional<Bucket> getBucket(String bucketName) {
        submit(getBucket.withInput(Input.AWS.TargetBucketName, bucketName));
        var bucket  = getBucket.outputAs(Output.AWS.Bucket, Bucket.class);
        return bucket;
    }

    private String getDataBucketName() {
        var prefix = "cj";
        var accountId = getCallerId.getOutputString(Output.AWS.Account);
        var region = getRegion();
        var bucketName = "%s-%s-%s".formatted(prefix, accountId, region);
        return bucketName;
    }

}
