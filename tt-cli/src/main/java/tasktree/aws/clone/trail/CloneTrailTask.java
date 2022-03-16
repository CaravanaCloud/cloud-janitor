package tasktree.aws.clone.trail;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudtrail.model.DescribeTrailsRequest;
import software.amazon.awssdk.services.cloudtrail.model.Trail;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class CloneTrailTask extends AWSTask {
    @Inject
    Logger log;

    @ConfigProperty(name = "tt.aws.trail", defaultValue = "aws-cloudtrail-name-to-clone")
    String trailName;

    public CloneTrailTask(){}

    @Inject
    public CloneTrailTask(Configuration config) {
        super(config);
    }

    @Override
    public void runSafe()  {
        log.info("Cloning trail [{}]", trailName);
        var cloudtrail = newCloudTrailClient();
        var describeTrailRequest = DescribeTrailsRequest.builder().trailNameList(trailName).build();
        try {
            var trails = cloudtrail.describeTrails(describeTrailRequest).trailList();
            if (trails.isEmpty()) {
                log.info("No trail found with name [{}]", trailName);
            } else {
                clone(trails.get(0));
            }
        }catch (Exception e) {
            log.error("Error cloning trail [{}]", trailName, e);
        }
    }

    private void clone(Trail trail) {
        log.info("Trail found. Cloning [{}]", trail.name());
        var bucket = trail.s3BucketName();
        var newBucket = bucket + "-clone";
        createBucket(newBucket);
        copyBucket(bucket, newBucket);
    }

    private void copyBucket(String bucket, String newBucket) {
        var s3tx = S3TransferManager.builder().s3ClientConfiguration(
                cfg -> cfg.region(getRegion())
        ).build();
        var s3 = newS3Client();
        var listObjects = ListObjectsV2Request.builder().bucket(bucket).build();
        var objects = s3.listObjectsV2Paginator(listObjects).contents().stream();
        objects.forEach(obj -> {
            log.debug("Copying object [{}] from bucket [{}] to bucket [{}]", obj.key(), bucket, newBucket);
        } );
    }

    private void createBucket(String newBucket) {
        var s3 = newS3Client();
        var headBucket = HeadBucketRequest.builder().bucket(newBucket).build();
        try{
            s3.headBucket(headBucket);
            log.info("Bucket [{}] already exists", newBucket);
        }catch (NoSuchBucketException e ){
            log.info("Bucket [{}] not exists, creating", newBucket);
            var mkBucket = CreateBucketRequest.builder().bucket(newBucket).build();
            s3.createBucket(mkBucket);
        }
    }


}
