package cloudjanitor.aws.sts;

import cloudjanitor.Output;
import cloudjanitor.TaskTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GetCallerIdentityTest extends TaskTest {
    @Inject
    GetCallerIdentityTask getCaller;

    @Test
    public void testGetCaller(){
        tasks.runTask(getCaller);
        var account = getCaller.findString(Output.AWS_ACCOUNT.name());
        assertNotNull(account);
        assertFalse(account.isEmpty());
    }
}
