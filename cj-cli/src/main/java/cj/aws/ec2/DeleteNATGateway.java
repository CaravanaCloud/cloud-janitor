package cj.aws.ec2;

import cj.Input;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.DeleteNatGatewayRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteNATGateway extends AWSWrite {

    @Override
    public void apply() {
        var natGatewayId = getInputString(Input.aws.targetNatGatewayId);
        debug("Deleting {}", natGatewayId);
        var deleteNat = DeleteNatGatewayRequest.builder().natGatewayId(natGatewayId).build();
        var ec2 = aws().ec2();
        ec2.deleteNatGateway(deleteNat);
        success();
    }
}
