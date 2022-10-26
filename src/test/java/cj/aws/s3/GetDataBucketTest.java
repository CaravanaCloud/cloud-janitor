package cj.aws.s3;

import cj.Output;
import cj.TaskTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.Bucket;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GetDataBucketTest extends TaskTest {

    @Inject
    GetDataBucket getDataBucket;

    @Test
    public void testGetDataBucket(){
        submit(getDataBucket);
        var bucket = getDataBucket.outputAs(Output.AWS.S3Bucket, Bucket.class);
        assertTrue(bucket.isPresent());
        log().debug("Got data bucket {}", bucket.get().name());
    }
}