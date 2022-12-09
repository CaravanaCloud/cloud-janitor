package cj.aws.s3;


import cj.TaskTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.Bucket;

import javax.inject.Inject;

import static cj.aws.AWSOutput.S3Bucket;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class GetDataBucketTest extends TaskTest {

    @Inject
    AWSGetBucketTask getDataBucket;

    @Test
    public void testGetDataBucket(){
        submit(getDataBucket);
        var bucket = getDataBucket.outputAs(S3Bucket, Bucket.class);
        assertTrue(bucket.isPresent());
        log().debug("Got data bucket {}", bucket.get().name());
    }
}