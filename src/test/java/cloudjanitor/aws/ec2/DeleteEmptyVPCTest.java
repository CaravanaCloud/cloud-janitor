package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.TaskTest;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;

import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static java.util.concurrent.TimeUnit.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeleteEmptyVPCTest extends TaskTest {
    @Inject
    Logger log;
    
    @Inject
    CreateVPC createVPC;

    @Inject
    FilterVPCs filterVPCs;

    @Inject
    CleanupVPCs deleteVpc;

    @Test
    public void testCreateAndDeleteVPC(){
        var vpcId = createVPC();
        if(vpcId == null)
            fail("Failed to create VPC");
        awaitCreate(vpcId);
        assertTrue(vpcExists(vpcId));
        deleteVPC(vpcId);
        awaitDelete(vpcId);
        assertFalse(vpcExists(vpcId));
    }

    private void awaitCreate(String vpcId) {
        await().atMost(30, SECONDS)
                .until(() -> vpcExists(vpcId));
    }

    private void awaitDelete(String vpcId) {
        await().atMost(30, SECONDS)
                .until(() -> ! vpcExists(vpcId));
    }

    private void deleteVPC(String vpcId) {
        deleteVpc.filterVPCs.input(Input.AWS.TargetVpcId, vpcId);
        tasks.runTask(deleteVpc);
    }

    @SuppressWarnings("unchecked")
    private boolean vpcExists(String vpcId) {
        filterVPCs.input(Input.AWS.TargetVpcId, vpcId);
        tasks.runTask(filterVPCs);
        var vpcs = filterVPCs.findAsList(Output.AWS.VPCMatch, Vpc.class);
        if (! vpcs.isEmpty()){
            var vpcExists = vpcs.get(0).vpcId().equals(vpcId);
            return vpcExists;
        } return false;
    }

    private String createVPC() {
        var vpcId = tasks
                .runTask(createVPC)
                .outputString(Output.AWS.VPCId);
        if (vpcId.isEmpty()) fail();
        var result = vpcId.get();
        log.debug("Created VPC {}", result);
        return vpcId.get();
    }

}