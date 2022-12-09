package cj.aws.s3;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import cj.aws.sts.GetCallerIdentityTask;
import cj.aws.sts.SimpleIdentity;
import cj.spi.Task;
import software.amazon.awssdk.services.s3.model.Bucket;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

import static cj.aws.AWSInput.bucketPolicy;
import static cj.aws.AWSInput.targetBucketName;
import static cj.aws.AWSOutput.CallerIdentity;
import static cj.aws.AWSOutput.S3Bucket;

@Dependent
@Named("aws-get-bucket")
@TaskMaturity(TaskMaturity.Level.experimental)
@TaskDescription("Get an S3 bucket")
public class AWSGetBucketTask extends AWSWrite {
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
        var bucketName = inputString(targetBucketName)
                .orElseGet(this::getDefaultBucketName);
        getOrCreateBucket(bucketName);
    }

    private void getOrCreateBucket(String bucketName) {
        var bucket = getBucket(bucketName);
        if (bucket.isPresent()){
            debug("Found S3 bucket {}", bucket.get().name());
            success(S3Bucket, bucket);
        }else {
            debug("Data bucket not found {}. Creating...", bucketName);
            var policy = inputString(bucketPolicy);
            var task = createBucket.withInput(targetBucketName, bucketName)
                            .withInput(AWSInput.bucketPolicy, policy);
            submit(task);
            debug("Checking if bucket was created");
            bucket = getBucket(bucketName);
            if (bucket.isPresent()){
                success(S3Bucket, bucket);
            }else {
                throw fail("Failed to create data bucket");
            }
        }
    }

    private Optional<Bucket> getBucket(String bucketName) {
        submit(getBucket.withInput(targetBucketName, bucketName));
        var bucket  = getBucket.outputAs(S3Bucket, Bucket.class);
        return bucket;
    }

    private String getDefaultBucketName() {
        var callerId = getCallerId.outputAs(CallerIdentity, SimpleIdentity.class);
        if (callerId.isEmpty()){
            throw fail("Could not find data bucket without caller id");
        } else {
            var accountId = callerId.get().accountId();
            var region = region();
            var bucketName = composeName("data", accountId, region.toString());
            return bucketName;
        }
    }

}
