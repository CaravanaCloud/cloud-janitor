package cj.aws.ec2;

import cj.CloudFormationTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@Disabled
public class DeleteVPCWithPrivateSubnetsTest extends CloudFormationTest {
     
    @Test
    public void test(){
        var vpcId = getOutput("VPC");
        cleanupVPC(vpcId);
        var vpcMatch = filterVPCs(vpcId);
        assertTrue(vpcMatch.isEmpty());
    }
}
