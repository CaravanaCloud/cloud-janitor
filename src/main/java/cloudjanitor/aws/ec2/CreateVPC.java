package cloudjanitor.aws.ec2;

import jdk.jfr.Name;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ec2.model.CreateVpcRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSClients;
import cloudjanitor.aws.AWSWrite;

import java.util.zip.ZipEntry;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Name("create-vpc")
@Dependent
public class CreateVPC extends AWSWrite {
    private static final String DEFAULT_CIDR = "10.0.0.0/16";

    @Override
    public void runSafe() {
        var ec2 = aws().getEC2Client();
        var req = CreateVpcRequest
                .builder()
                .cidrBlock(inputString(Input.AWS.VpcCIDR, DEFAULT_CIDR))
                .build();
        var resp = ec2.createVpc(req);
        var vpc = resp.vpc();
        var vpcId = vpc.vpcId();
        success(Output.AWS.VPCId, vpcId);
        
        log().debug("VPC {}/{} created", aws().getRegion(), vpcId);
    }
}
