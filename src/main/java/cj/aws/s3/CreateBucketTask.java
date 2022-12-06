package cj.aws.s3;

import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

import javax.enterprise.context.Dependent;

import static cj.aws.AWSInput.targetBucketName;

@Dependent
public class CreateBucketTask extends AWSWrite {
    public void  apply(){
        try(var s3 = aws().s3()){
            createBucket(s3);
        }
    }

    private void createBucket(S3Client s3) {
        var bucketName = getInputString(targetBucketName);
        if (! isValidBucketName(bucketName)){
            throw fail("Invalid bucket name " + bucketName);
        }else {
            var req = CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .build();
            var resp = s3.createBucket(req);
            var location = resp.location();
            info("Bucket created {} {}", bucketName, location);
            var policyIn = inputString(AWSInput.bucketPolicy);
            if(policyIn.isPresent()){
                var policy = policyIn.get();
                var policyReq = PutBucketPolicyRequest
                        .builder()
                        .bucket(bucketName)
                        .policy(policy)
                        .build();
                s3.putBucketPolicy(policyReq);
                debug("Bucket policy set [{}]", policy.length());
            }

        }
    }

    private boolean isValidBucketName(String bucketName) {
        int nameLength = bucketName.length();
        boolean isValid = nameLength > 3 && bucketName.length() < 64;
        return isValid;
    }
}
