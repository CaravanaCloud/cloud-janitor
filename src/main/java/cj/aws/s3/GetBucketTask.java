package cj.aws.s3;

import cj.Output;
import cj.aws.AWSTask;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;

import javax.enterprise.context.Dependent;

import static cj.aws.AWSInput.*;

@Dependent
public class GetBucketTask extends AWSTask {
    @Override
    public void apply() {
        var bucketName = getInputString(targetBucketName);
        var s3 = aws().s3();
        var req = ListBucketsRequest.builder().build();
        var buckets = s3.listBuckets(req).buckets();
        var match = buckets
                .stream()
                .parallel()
                .filter(b -> bucketName.equals(b.name()))
                .findAny();
        if (match.isPresent()){
            debug("Found bucket {}", bucketName);
            success(Output.aws.S3Bucket, match.get());
        }else{
            debug("Bucket not found");
            success();
        }
    }
}
