package cj.aws.ec2.create;

import cj.Output;
import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.CreateVpcRequest;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import static cj.aws.AWSOutput.*;

@Named("create-vpc")
@Dependent
public class CreateVPC extends AWSWrite {
    private static final String DEFAULT_CIDR = "10.0.0.0/16";

    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var req = CreateVpcRequest
                .builder()
                .cidrBlock(getInputString(AWSInput.vpcCIDR, DEFAULT_CIDR))
                .build();
        var resp = ec2.createVpc(req);
        var vpc = resp.vpc();
        var vpcId = vpc.vpcId();
        success(VPCId, vpcId);
        
        debug("VPC {} / {} created", aws().region(), vpcId);
    }
}
