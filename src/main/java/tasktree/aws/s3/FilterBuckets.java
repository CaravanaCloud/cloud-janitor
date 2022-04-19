package tasktree.aws.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import tasktree.aws.AWSFilter;
import tasktree.aws.ec2.DeleteVpc;
import tasktree.aws.ec2.FilterVPCs;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.stream.Stream;

@Dependent
public class FilterBuckets extends AWSFilter<Bucket> {

    private boolean matchPrefix(Bucket bucket) {
        var prefix = getAwsCleanupPrefix();
        var match = bucket.name().startsWith(prefix);
        return match;
    }

    private List<Bucket> findAll(){
        var s3 = aws().newS3Client(getRegion());
        var request = ListBucketsRequest.builder().build();
        var buckets = s3.listBuckets().buckets();
        if (! getAwsCleanupPrefix().isEmpty()){
            buckets = buckets.stream().filter(this::matchPrefix).toList();
        }
        if (getTargetName() != null){
            buckets = buckets.stream().filter(this::matchTargetName).toList();
        }
        return buckets;
    }

    private String getTargetName() {
        var targetName = get("s3.bucket.name");
        return targetName;
    }

    private boolean matchTargetName(Bucket bucket) {
        var match = bucket.name().equals(getTargetName());
        return match;
    }

    @Override
    protected List<Bucket> filterResources() {
        var matches = findAll().stream().toList();
        if (getAwsCleanupPrefix() != null && ! getAwsCleanupPrefix().isEmpty())
            matches = matches.stream().filter(this::matchPrefix).toList();
        return matches;
    }


    @Override
    public Stream<Task> mapSubtasks(Bucket bucket) {
        var bucketName = bucket.name();
        return Stream.of(
                new DeleteBucket(bucketName)
        );
    }

    @Override
    protected String getResourceType() {
        return "Bucket";
    }

}
