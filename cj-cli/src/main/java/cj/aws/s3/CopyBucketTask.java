package cj.aws.s3;

import cj.aws.AWSWrite;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Named("copy-bucket")
@Dependent
public class CopyBucketTask extends AWSWrite {
    /*
    @ConfigProperty(name = "tt.s3.from.bucket", defaultValue = "??")
    String fromBucket;

    @ConfigProperty(name = "tt.s3.from.region", defaultValue = "us-east-1")
    String fromRegion;

    @ConfigProperty(name = "tt.s3.to.bucket", defaultValue = "??")
    String toBucket;

    @ConfigProperty(name = "tt.s3.to.region", defaultValue = "us-east-1")
    String toRegion;

    @Inject
    AWSClients aws;

    @Override
    public void runSafe() {
        createIfNotExists();
        // copy(fromBucket, toBucket);
        // var s3tm = S3TransferManager.builder().build();
    }

    private void createIfNotExists() {
        var s3_to = aws.newS3Client(getToRegion());
        try {
            var req = HeadBucketRequest.builder()
                    .bucket(toBucket)
                    .build();
            var resp = s3_to.headBucket(req);
            log().info("Target bucket exists {}", resp);
        }catch(Exception ex){
            log().info("Target bucket does not exist");
        }
    }

    private Region getToRegion() {
        return Region.of(toRegion);
    }

     */
}
