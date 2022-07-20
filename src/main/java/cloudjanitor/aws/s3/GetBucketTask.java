package cloudjanitor.aws.s3;

import cloudjanitor.Output;
import cloudjanitor.aws.AWSTask;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;

import javax.enterprise.context.Dependent;

import static cloudjanitor.Input.AWS.TargetBucketName;

@Dependent
public class GetBucketTask extends AWSTask {
    @Override
    public void apply() {
        var bucketName = getInputString(TargetBucketName);
        var s3 = aws().s3();
        var req = ListBucketsRequest.builder().build();
        var buckets = s3.listBuckets(req).buckets();
        var match = buckets
                .stream()
                .parallel()
                .filter(b -> bucketName.equals(b.name()))
                .findAny();
        if (match.isPresent()){
            log().debug("Found bucket {}", bucketName);
            success(Output.AWS.S3Bucket, match.get());
        }else{
            log().debug("Bucket not found");
            success();
        }
    }
}
