package cloudjanitor.aws.s3;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteBucket extends AWSWrite {

    @Override
    public void apply() {
        var resource = getInputString(Input.AWS.TargetBucketName);
        debug("Deleting Bucket [{}]", resource);
        var request = DeleteBucketRequest.builder().bucket(resource).build();
        aws().s3().deleteBucket(request);
    }

}
