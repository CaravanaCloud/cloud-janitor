package cj.aws.s3;

import cj.aws.AWSTask;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;

import javax.enterprise.context.Dependent;

import static cj.aws.AWSInput.targetBucketName;
import static cj.aws.AWSOutput.S3Bucket;

@Dependent
public class GetBucketTask extends AWSTask {
    @Override
    public void apply() {
        var bucketName = getInputString(targetBucketName);
        var s3 = aws().s3();
        var req = ListBucketsRequest
                .builder()
                .build();
        var buckets = s3.listBuckets(req).buckets();
        var match = buckets
                .stream()
                .filter(b -> bucketName.equals(b.name()))
                .findAny();
        if (match.isPresent()) {
            debug("Found bucket {}", bucketName);
            if (headBucket(bucketName))
                success(S3Bucket, match.get());
        } else {
            debug("Bucket not found");
            success();
        }
    }

    private boolean headBucket(String bucketName) {
        try (var s3 = aws().s3()) {
            debug("Heading bucket {}", bucketName);
            var req = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            var res = s3.headBucket(req);
            return true;
        } catch (Exception ex) {
            debug("Failed to head bucket");
            return false;
        }

    }
}