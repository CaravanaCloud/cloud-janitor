package cloudjanitor.aws.s3;

import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import javax.enterprise.context.Dependent;

import static cloudjanitor.Input.AWS.targetBucketName;

@Dependent
public class CreateBucketTask extends AWSWrite {
    public void  apply(){
        var s3 = aws().s3();
        var bucketName = getInputString(targetBucketName);
        if (! isValidBucketName(bucketName)){
            fail("Invalid bucket name " + bucketName);
        }else {
            var req = CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .build();
            var resp = s3.createBucket(req);
            var location = resp.location();
            info("Bucket created {} {}", bucketName, location);

        }
    }

    private boolean isValidBucketName(String bucketName) {
        int nameLength = bucketName.length();
        boolean isValid = nameLength > 3 && bucketName.length() < 64;
        return isValid;
    }
}
