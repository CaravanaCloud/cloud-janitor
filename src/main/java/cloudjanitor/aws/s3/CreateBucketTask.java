package cloudjanitor.aws.s3;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import javax.enterprise.context.Dependent;

import static cloudjanitor.Input.AWS.TargetBucketName;

@Dependent
public class CreateBucketTask extends AWSWrite {
    public void  apply(){
        var s3 = aws().s3();
        var bucketName = getInputString(TargetBucketName);
        var req = CreateBucketRequest
                .builder()
                .bucket(bucketName)
                .build();
        var resp = s3.createBucket(req);
        log().info("Bucket created {}", bucketName);
    }
}
