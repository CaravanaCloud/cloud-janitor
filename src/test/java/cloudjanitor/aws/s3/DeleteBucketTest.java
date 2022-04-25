package cloudjanitor.aws.s3;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.util.List;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static java.util.concurrent.TimeUnit.*;

@QuarkusTest
public class DeleteBucketTest {
    @Inject
    Logger log;

    @Inject
    CreateBucket createBucket;

    @Inject
    FilterBuckets filterBuckets;

    @Inject
    DeleteBucket deleteBucket;

    @Test
    public void testDeleteBucket(){
        var bucketName = createBucket();
        assertTrue(bucketExists(bucketName));
        deleteBucket(bucketName);
        awaitDelete(bucketName);
        assertFalse(bucketExists(bucketName));
    }

    private void deleteBucket(String bucketName) {
        deleteBucket.setResources(List.of(bucketName));
        deleteBucket.runSafe();
        log.info("Bucket deleted {}", bucketName);
    }

    private void awaitDelete(String bucketName) {
        await().atMost(10, SECONDS)
                .until(() -> ! bucketExists(bucketName));
    }

    private String createBucket() {
        var name = "rhnb-"+System.currentTimeMillis();
        createBucket.set("s3.bucket.name",name);
        createBucket.runSafe();
        log.info("Bucket created {}", name);
        return name;
    }

    private boolean bucketExists(String bucketName){
        filterBuckets.set("s3.bucket.name", bucketName);
        filterBuckets.runSafe();
        var buckets = filterBuckets.getResources();
        var exists = buckets.size() == 1
                && buckets.get(0).name().equals(bucketName);
        log.info("Bucket exists [{}]? {}", bucketName, exists);
        return exists;
    }
}
