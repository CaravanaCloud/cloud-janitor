package cj.aws.sts;

import cj.TaskTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static cj.Output.AWS.CallerIdentity;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class GetCallerIdentityTaskTest extends TaskTest {
    @Inject
    GetCallerIdentityTask getCaller;

    @Test
    public void testGetCaller(){
        submit(getCaller);
        var account = getCaller.outputString(CallerIdentity);
        assertNotNull(account);
        assertFalse(account.isEmpty());
    }
}
