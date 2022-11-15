package cj.aws.s3;

import cj.Input;
import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteBucket extends AWSWrite {

    @Override
    public void apply() {
        var resource = getInputString(AWSInput.targetBucketName);
        debug("Deleting Bucket [{}]", resource);
        var request = DeleteBucketRequest.builder().bucket(resource).build();
        aws().s3().deleteBucket(request);
    }

}
