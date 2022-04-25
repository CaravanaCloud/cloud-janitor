package cloudjanitor.aws.s3;

import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import cloudjanitor.aws.AWSCleanup;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.stream.Stream;

@Dependent
public class DeleteBucket extends AWSCleanup<String/*BucketName*/> {
    public DeleteBucket(){}

    public DeleteBucket(String resource) {
        super(resource);
    }

    @Override
    public void cleanup(String resource) {
        log().debug("Deleting Bucket [{}]", resource);
        var request = DeleteBucketRequest.builder().bucket(resource).build();
        aws().newS3Client(getRegion()).deleteBucket(request);
    }

    @Override
    protected String getResourceType() {
        return "Bucket";
    }

    @Override
    public Stream<Task> mapSubtasks(String bucketName) {
        return Stream.of(
                //TODO: Filter Objects, Versions, Delete Markers..
        );
    }
}
