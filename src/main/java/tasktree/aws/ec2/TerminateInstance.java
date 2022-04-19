package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import tasktree.aws.AWSCleanup;

import java.util.Optional;

public class TerminateInstance extends AWSCleanup<Instance> {
    public TerminateInstance(Instance instance) {
        super(instance);
    }

    @Override
    public void cleanup(Instance resource) {
        var state = resource.state().nameAsString().toLowerCase();
        if (state.toString().equals("running")) {
            log().debug("Terminating instance {}", resource);
            var terminateInstance = TerminateInstancesRequest.builder()
                    .instanceIds(resource.instanceId())
                    .build();
            var ec2 = newEC2Client();
            ec2.terminateInstances(terminateInstance);
            waitForTermination(resource);
        } else {
            log().trace("Not terminating instance {} {}", state, resource);
        }
    }

    private void waitForTermination(Instance resource) {
        var retries = 5;
        var wait = false;
        do {
            var instance = lookupInstance(resource.instanceId());
            var state = instance.state().toString();
            var waitTermination = switch (state) {
                case "terminated" -> false;
                default -> true;
            };
            retries--;
            wait = waitTermination && retries > 0;
            if (wait){
                log().info("Waiting for termination of {} ({})", instance.instanceId(), instance.state());
                sleep();
            }
        }while(wait);
    }

    private void sleep() {
        try {
            Thread.sleep(5_000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Instance lookupInstance(String instanceId) {
        var ec2 = newEC2Client();
        var nextToken = (String) null ;
        try {
            do {
                var request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                var response = ec2.describeInstances(request);

                for (var reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        if(instance.instanceId().equals(instanceId))
                            return instance;
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
    }

    @Override
    protected String getResourceType() {
        return "EC2 Instance";
    }

    @Override
    public Optional<Long> getWaitAfterRun() {
        return Optional.of(30_000L);
    }
}
