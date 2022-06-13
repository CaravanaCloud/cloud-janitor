package cloudjanitor.aws.ec2;

import cloudjanitor.CloudFormationTest;
import cloudjanitor.Output;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DeleteVPCWithPrivateSubnetsTest extends CloudFormationTest {
     
    @Test
    public void test(){
        var vpcId = getOutput("VPC");
        cleanupVPC(vpcId);
        var vpcMatch = filterVPCs(vpcId);
        assertTrue(vpcMatch.isEmpty());
    }
}
