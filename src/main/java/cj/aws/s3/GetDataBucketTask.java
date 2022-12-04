package cj.aws.s3;

import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import cj.aws.sts.SimpleIdentity;
import cj.aws.sts.GetCallerIdentityTask;
import cj.spi.Task;
import software.amazon.awssdk.services.s3.model.Bucket;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

import static cj.aws.AWSOutput.*;

@Dependent
@Named("aws-get-data-bucket")
public class GetDataBucketTask extends AWSWrite {
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
        getOrCreateBucket(bucketName);
    }

    private void getOrCreateBucket(String bucketName) {
        var bucket = getBucket(bucketName);
        if (bucket.isPresent()){
            debug("Found data bucket {}", bucket.get().name());
            success(S3Bucket, bucket);
        }else {
            debug("Data bucket not found {}. Creating...", bucketName);
            submit(createBucket.withInput(AWSInput.targetBucketName, bucketName));
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
        submit(getBucket.withInput(AWSInput.targetBucketName, bucketName));
        var bucket  = getBucket.outputAs(S3Bucket, Bucket.class);
        return bucket;
    }

    private String getDataBucketName() {
        var prefix = config().getNamingPrefix();
        var callerId = getCallerId.outputAs(CallerIdentity, SimpleIdentity.class);
        if (callerId.isEmpty()){
            throw fail("Could not find data bucket without caller id");
        } else {
            var accountId = callerId.get().accountId();
            var region = region();
            var bucketName = "%s-%s-%s".formatted(prefix, accountId, region);
            return bucketName;
        }
    }

}
