package cloudjanitor.aws.sts;

import cloudjanitor.TaskTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class GetCallerIdentityTest extends TaskTest {
    @Inject
    GetCallerIdentityTask getCaller;

    @Test
    public void testGetCaller(){
        tasks.runTask(getCaller);
        var account = getCaller.findString("aws.account");
        assertTrue(account != null);
        assertTrue(! account.isEmpty());
    }
}
