package tasktree.aws.ec2;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tasktree.Configuration;
import tasktree.aws.AWSClients;

import javax.inject.Inject;

import java.util.List;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static java.time.Duration.*;
import static java.util.concurrent.TimeUnit.*;

@QuarkusTest
public class CreateVPCTest {
    @Inject
    Configuration config;

    @Inject
    CreateVPC createVPC;

    @Inject
    FilterVPCs filterVPCs;

    @Inject
    DeleteVpc deleteVpc;

    @Test
    public void testCreateAndDeleteVPC(){
        var vpcId = createVPC();
        awaitCreate(vpcId);
        deleteVPC(vpcId);
        awaitDelete(vpcId);
        assertFalse(vpcExists(vpcId));
    }

    private void awaitDelete(String vpcId) {
        await().atMost(10, SECONDS)
                .until(() -> ! vpcExists(vpcId));
    }

    private void awaitCreate(String vpcId) {
        await().atMost(10, SECONDS)
                .until(() -> vpcExists(vpcId));
    }

    private void deleteVPC(String vpcId) {
        deleteVpc.setResources(List.of(vpcId));
        deleteVpc.runSafe();
    }

    private boolean vpcExists(String vpcId) {
        filterVPCs.set("vpc.id",vpcId);
        filterVPCs.runSafe();
        var vpcs = filterVPCs.getResources();
        var vpcExists = vpcs.size() == 1 && vpcs.get(0).vpcId().equals(vpcId);
        return vpcExists;
    }

    private String createVPC() {
        createVPC.runSafe();
        var result = createVPC.getResult();
        return result.get("vpc.id");
    }

}