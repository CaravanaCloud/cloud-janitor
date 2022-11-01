package cj;

import cj.aws.ec2.CleanupVPCs;
import cj.aws.ec2.FilterVPCs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import software.amazon.awssdk.services.cloudformation.model.*;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CloudFormationTest extends TaskTest{
    @Inject
    Logger log;

    String stackName;

    @Inject
    CleanupVPCs cleanupVPCs;

    @Inject
    FilterVPCs filterVPCs;


    @BeforeAll
    public void beforeALl(){
        createStack();
    }

    @AfterAll
    public void afterAll(){
        if (!retainStackAfterTest())
            deleteStack();
    }

    protected boolean retainStackAfterTest() {
        return false;
    }

    protected void createStack(){
        log.info("Creating stack {} on {}", getStackName(), aws().getRegion());
        var cf = aws().cloudFormation();
        var stackName = getStackName();
        var createReq = CreateStackRequest.builder()
                .stackName(getStackName())
                .capabilities(Capability.CAPABILITY_IAM)
                .templateBody(templateBody(getSimpleName()))
                .build();
        var stackId = cf.createStack(createReq).stackId();
        var waiting = false;
        var status = "";
        do {
            var describeReq = DescribeStacksRequest
                    .builder()
                    .stackName(stackName)
                    .build();
            var stacks =(List<Stack>) new ArrayList<Stack>();
            try {
                stacks = cf.describeStacks(describeReq).stacks();
            }catch (Exception e){
                //TODO: awaitility this
                log.warn("Failed to describe stack..");
            }
            if (! stacks.isEmpty()){
                var stack = stacks.get(0);
                status = stack.stackStatus().toString();
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
        if ("ROLLBACK_COMPLETE".equals(status)){
            fail("Stack failed to create");
        }
        System.out.println("CREATE DONE");
    }

    protected String getStackName() {
        if (stackName == null) {
            var simpleName = getSimpleName();
            var runId = tasks.getExecutionId();
            stackName = simpleName+"-"+runId;
        }
        return stackName;
    }

    private String templateBody(String templateName) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        var resourceName = "cloudformation/"+templateName+".yaml";
        InputStream is = classloader.getResourceAsStream(resourceName);
        if(is == null){
            fail("Resource not found: "+resourceName);
            throw new RuntimeException("Resource not found: "+resourceName);
        }else {
            String body = null;
            try {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return body;
        }
    }

    private String getSimpleName() {
        return this.getClass().getSimpleName();
    }

    protected void deleteStack(){
        var cf = aws().cloudFormation();
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
                    waiting = false;
                }
            }catch(CloudFormationException ex){
                System.out.println("Failed to describe stack, consider it gone.");
                waiting = false;
            }
        } while (waiting);
        System.out.println("DELETE DONE");
    }

    protected Optional<String> output(String name) {
        var cf = aws().cloudFormation();
        var stackName = getStackName();
        var req = DescribeStacksRequest.builder()
                .stackName(stackName)
                .build();
        var stacks = cf.describeStacks(req).stacks();
        if (! stacks.isEmpty()) {
            var stack = stacks.get(0);
            var outs =
                    stack.outputs()
                            .stream()
                            .toList();
            var out = outs.stream()
                            .filter(output -> output.outputKey().equals(name))
                            .map(output -> output.outputValue())
                            .findAny();
            return out;
        }
        return Optional.empty();
    }

    protected String getOutput(String name) {
        var out = output(name);
        if (out.isEmpty()){
            fail("Expected output name " + name + " not found in stack "+getStackName());
        }
        return out.orElseThrow();
    }

    protected List<Vpc> filterVPCs(String vpcId) {
        filterVPCs.setTargetVPC(vpcId);
        tasks.submit(filterVPCs);
        var matches = filterVPCs.outputList(Output.aws.VPCMatch, Vpc.class);
        log.debug("filterVPCs {} finished", vpcId);
        return matches;
    }

    protected void cleanupVPC(String vpcId) {
        cleanupVPCs.setTargetVPC(vpcId);
        tasks.submit(cleanupVPCs);
        log.debug("cleanupVPC {} finished",vpcId);
    }



}
