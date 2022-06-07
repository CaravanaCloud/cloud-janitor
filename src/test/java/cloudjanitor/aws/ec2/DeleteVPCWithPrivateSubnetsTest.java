package cloudjanitor.aws.ec2;

import cloudjanitor.CloudFormationTest;
import cloudjanitor.Output;
import io.quarkus.test.junit.QuarkusTest;
import software.amazon.awssdk.services.ec2.model.Vpc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DeleteVPCWithPrivateSubnetsTest extends CloudFormationTest {
     @Inject
     Logger log;

     @Inject
     CleanupVPCs cleanupVPCs;

     @Inject
     FilterVPCs filterVPCs;

    @Test
    public void test(){
        var vpcId = getOutput("VPC");
        cleanupVPC(vpcId);
        var resources = filterVPCs(vpcId);
        assertTrue(resources.isEmpty());
    }

    private List<Vpc> filterVPCs(String vpcId) {
        filterVPCs.setTargetVPC(vpcId);
        tasks.runTask(filterVPCs);
        var matches = filterVPCs.findAsList(Output.AWS.VPCMatch, Vpc.class);
        log.debug("filterVPCs {} finished",vpcId);
        return matches;
    }

    private void cleanupVPC(String vpcId) {
        cleanupVPCs.filterVPCs.setTargetVPC(vpcId);
        tasks.runTask(cleanupVPCs);
        log.debug("cleanupVPC {} finished",vpcId);
    }
}
