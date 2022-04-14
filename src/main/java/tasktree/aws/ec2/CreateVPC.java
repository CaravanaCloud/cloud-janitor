package tasktree.aws.ec2;

import jdk.jfr.Name;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ec2.model.CreateVpcPeeringConnectionRequest;
import software.amazon.awssdk.services.ec2.model.CreateVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import tasktree.aws.AWSClients;
import tasktree.aws.AWSWrite;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Name("create-vpc")
@Dependent
public class CreateVPC extends AWSWrite<Vpc> {
    @Inject
    AWSClients aws;

    @ConfigProperty(name = "tt.vpc.cidr", defaultValue = "10.0.0.0/24")
    String vpcCIDR;

    @Override
    public void runSafe() {
        var ec2 = aws.newEC2Client(getRegionOrDefault());
        var req = CreateVpcRequest
                .builder()
                .cidrBlock(vpcCIDR)
                .build();
        var resp = ec2.createVpc(req);
        var vpcId = resp.vpc().vpcId();
        log().info("VPC {} created", vpcId);
        success("vpc.id", vpcId);
    }
}
