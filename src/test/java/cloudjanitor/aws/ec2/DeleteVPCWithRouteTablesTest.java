package cloudjanitor.aws.ec2;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cloudjanitor.CloudFormationTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class DeleteVPCWithRouteTablesTest extends CloudFormationTest {
    @Test
    public void test(){
        var vpcId = getOutput("VPC");
        cleanupVPC(vpcId);
        var vpcMatch = filterVPCs(vpcId);
        assertTrue(vpcMatch.isEmpty());
    }
}
