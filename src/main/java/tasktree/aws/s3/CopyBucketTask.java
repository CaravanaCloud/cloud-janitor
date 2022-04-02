package tasktree.aws.s3;

import com.amazonaws.s3.model.Bucket;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tasktree.aws.cleanup.AWSWrite;

public class CopyBucketTask extends AWSWrite<Bucket> {
    @ConfigProperty(name = "tt.s3.from")
    String fromBucket;

    @ConfigProperty(name = "tt.s3.to")
    String toBucket;

    @Override
    public void runSafe() {
        // createIfNotExists(toBucket);
        // copy(fromBucket, toBucket);
    }
}
