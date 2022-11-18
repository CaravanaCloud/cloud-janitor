package cj.aws.ec2.delete;

import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.Address;
import software.amazon.awssdk.services.ec2.model.DisassociateAddressRequest;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;

import javax.enterprise.context.Dependent;

@Dependent
public class ReleaseAddress extends AWSWrite {

    @Override
    public void apply() {
        var eip = getInput(AWSInput.address, Address.class);
        debug("Releasing address {}", eip.publicIp());
        var associationId = eip.associationId();
        if (associationId != null && !associationId.isEmpty()) {
            debug("Disassociating address {} from {}", eip.publicIp(), associationId);
            var disassociateRequest = DisassociateAddressRequest.builder()
                    .associationId(associationId)
                    .build();
            aws().ec2().disassociateAddress(disassociateRequest);
        }
        var request = ReleaseAddressRequest.builder()
                .allocationId(eip.allocationId())
                .build();
        aws().ec2().releaseAddress(request);
        success();
    }

}
