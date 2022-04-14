package tasktree.aws.s3;

import com.amazonaws.s3.model.Bucket;
import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import tasktree.aws.AWSDelete;
import tasktree.aws.ec2.*;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.stream.Stream;

@Dependent
public class DeleteBucket extends AWSDelete<String/*BucketName*/> {
    public DeleteBucket(){}

    public DeleteBucket(String resource) {
        super(resource);
    }

    @Override
    public void cleanup(String resource) {
        log().debug("Deleting Bucket [{}]", resource);
        var request = DeleteBucketRequest.builder().bucket(resource).build();
        newS3Client().deleteBucket(request);
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
