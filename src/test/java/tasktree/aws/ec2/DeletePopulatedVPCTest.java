package tasktree.aws.ec2;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import software.amazon.awssdk.services.cloudformation.model.*;
import tasktree.Configuration;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeletePopulatedVPCTest {

    @Inject
    Configuration cfg;

    @BeforeAll
    public void beforeALl(){
        var cf = cfg.aws().newCloudFormationClient();
        var stackName = getStackName();
        //TODO: Check resources are created
        var createReq = CreateStackRequest.builder()
                .stackName(getStackName())
                .capabilities(Capability.CAPABILITY_IAM)
                .templateBody(templateBody(getSimpleName()))
                .build();
        var stackId = cf.createStack(createReq).stackId();
        System.out.println("Stack ID "+stackId);
        var waiting = false;
        do {
            var describeReq = DescribeStacksRequest
                    .builder()
                    .stackName(stackName)
                    .build();
            var stacks = cf.describeStacks(describeReq)
                    .stacks();
            if (! stacks.isEmpty()){
                var stack = stacks.get(0);
                var status = stack.stackStatus().toString();
                waiting = switch(status) {
                   case "CREATE_COMPLETE",
                           "CREATE_FAILED",
                           "ROLLBACK_COMPLETE" -> false;
                   default -> true;
                };
                System.out.println("Stack "+stackName+" is "+status);
                if (waiting){
                    System.out.println("Waiting create...");
                    try {
                        Thread.sleep(15_000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                System.out.println("Stack not found");
            }
        } while (waiting);
        System.out.println("CREATE DONE");
    }

    @AfterAll
    private void afterAll(){
        var cf = cfg.aws().newCloudFormationClient();
        var stackName = getStackName();
        var deleteReq = DeleteStackRequest.builder()
                .stackName(stackName)
                .build();
        cf.deleteStack(deleteReq);
        var waiting = false;
        do {
            var describeReq = DescribeStacksRequest
                    .builder()
                    .stackName(stackName)
                    .build();
            try {
                var stacks = cf.describeStacks(describeReq)
                        .stacks();
                if (!stacks.isEmpty()) {
                    var stack = stacks.get(0);
                    var status = stack.stackStatus().toString();
                    waiting = switch (status) {
                        case "DELETE_COMPLETE",
                                "DELETE_FAILED",
                                "ROLLBACK_COMPLETE" -> false;
                        default -> true;
                    };
                    System.out.println("Stack " + stackName + " is " + status);
                    if (waiting) {
                        System.out.println("Waiting delete...");
                        try {
                            Thread.sleep(15_000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Stack not found");
                }
            }catch(CloudFormationException ex){
                System.out.println("Failed to describe stack, consider it gone.");
            }
        } while (waiting);
        System.out.println("DELETE DONE");
    }

    private String getStackName() {
        var simpleName = getSimpleName();
        var runId = cfg.getExecutionId();
        var stacKName = runId+"-"+simpleName;
        return stacKName;
    }

    private String getSimpleName() {
        return DeletePopulatedVPCTest.class.getSimpleName();
    }

    private String templateBody(String templateName) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        var resourceName = "cloudformation/"+templateName+".yaml";
        InputStream is = classloader.getResourceAsStream(resourceName);
        String body = null;
        try {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return body;
    }

    @Test
    public void testDeletePopulatedVPC(){
        System.out.println("Delete that vpc");
        System.out.println("Verify it's emtpy");
    }

}
