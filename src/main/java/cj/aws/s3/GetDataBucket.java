package cj.aws.s3;

import cj.Input;
import cj.Output;
import cj.aws.AWSWrite;
import cj.aws.sts.CallerIdentity;
import cj.aws.sts.GetCallerIdentityTask;
import cj.spi.Task;
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
            debug("Found data bucket {}", bucket.get().name());
            success(Output.AWS.S3Bucket, bucket);
        }else {
            debug("Data bucket not found {}. Creating...", bucketName);
            submit(createBucket.withInput(Input.AWS.targetBucketName, bucketName));
            debug("Checking if bucket was created");
            bucket = getBucket(bucketName);
            if (bucket.isPresent()){
                success(Output.AWS.S3Bucket, bucket);
            }else {
                fail("Failed to create data bucket");
            }
        }

    }

    private Optional<Bucket> getBucket(String bucketName) {
        submit(getBucket.withInput(Input.AWS.targetBucketName, bucketName));
        var bucket  = getBucket.outputAs(Output.AWS.S3Bucket, Bucket.class);
        return bucket;
    }

    private String getDataBucketName() {
        var prefix = "cj";
        var callerId = getCallerId.outputAs(Output.AWS.CallerIdentity, CallerIdentity.class);
        if (callerId.isEmpty()){
            throw fail("Could not find data bucket without caller id");
        } else {
            var accountId = callerId.get().accountId();
            var region = getRegion();
            var bucketName = "%s-%s-%s".formatted(prefix, accountId, region);
            return bucketName;
        }
    }

}
