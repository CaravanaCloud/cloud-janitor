package cloudjanitor.aws.ec2;

import cloudjanitor.CloudFormationTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

//TODO: Test VPC with empty subnet
@QuarkusTest
public class DeleteVPCWithPrivateSubnetsTest extends CloudFormationTest {
     @Inject
     Logger log;

     @Inject
     CleanupVPCs cleanupVPCs;

     @Inject
     FilterVPCs filterVPCs;

    @Test
    public void testDeletePopulatedVPC(){
        log.debug("Cleanup populated VPC test initialized");
        var vpcId = getOutput("VPC");
        cleanupVPC(vpcId);
        log.debug("Verifying that the target VPC does not exist");
        var resources = filterVPCs(vpcId);
        assertTrue(resources.isEmpty());
    }

    private List filterVPCs(String vpcId) {
        filterVPCs.setTargetVPC(vpcId);
        tasks.runTask(filterVPCs);
        var matches = filterVPCs.findAsList("aws.vpc.matches");
        log.debug("filterVPCs {} finished",vpcId);
        return matches;
    }

    private void cleanupVPC(String vpcId) {
        cleanupVPCs.filterVPCs.setTargetVPC(vpcId);
        tasks.runTask(cleanupVPCs);
        log.debug("cleanupVPC {} finished",vpcId);
    }
}
