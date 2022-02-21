package tasktree.aws;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ClientProducer {
    public static Ec2Client newEC2Client() {
        var defaultRegion = Region.US_EAST_1;
        return newEC2Client(defaultRegion);
    }

    public static Ec2Client newEC2Client(Region region) {
        return Ec2Client.builder().region(region).build();
    }
}
