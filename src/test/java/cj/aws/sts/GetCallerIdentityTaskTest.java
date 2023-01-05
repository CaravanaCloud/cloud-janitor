package cj.aws.sts;

import cj.TaskTest;
import cj.aws.AWSIdentity;
import cj.aws.AWSIdentityInfo;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GetCallerIdentityTaskTest extends TaskTest {
    @Inject
    GetCallerIdentityTask getCallerId;

    @Test
    public void testGetCaller(){
        submit(getCallerId);
        var result = getCallerId.output(AWSIdentityInfo.class);
        assertNotNull(result);
    }
}
