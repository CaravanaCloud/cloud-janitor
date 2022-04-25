package cloudjanitor.aws.s3;

import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import cloudjanitor.aws.AWSClients;
import cloudjanitor.aws.AWSWrite;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class CreateBucket extends AWSWrite<Bucket> {
    @Inject
    AWSClients aws;


    @Override
    public void runSafe() {
        var s3 = aws.newS3Client(getRegionOrDefault());
        var bucketName = get("s3.bucket.name");
        var req = CreateBucketRequest
                .builder()
                .bucket(bucketName)
                .build();
        var resp = s3.createBucket(req);
        log().info("Bucket created {}", bucketName);
    }
}
