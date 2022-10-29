package cj.aws.ec2;

import software.amazon.awssdk.services.ec2.model.CreateVpcRequest;
import cj.Input;
import cj.Output;
import cj.aws.AWSWrite;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Named("create-vpc")
@Dependent
public class CreateVPC extends AWSWrite {
    private static final String DEFAULT_CIDR = "10.0.0.0/16";

    @Override
    public void apply() {
        var ec2 = aws().ec2();
        var req = CreateVpcRequest
                .builder()
                .cidrBlock(getInputString(Input.AWS.vpcCIDR, DEFAULT_CIDR))
                .build();
        var resp = ec2.createVpc(req);
        var vpc = resp.vpc();
        var vpcId = vpc.vpcId();
        success(Output.AWS.VPCId, vpcId);
        
        debug("VPC {} / {} created", aws().getRegion(), vpcId);
    }
}
