package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSTask;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cloudjanitor.Output.AWS.*;

@Dependent
public class CleanupVPCs extends AWSTask {

    @Inject
    FilterVPCs filterVPCs;

    @Inject
    Instance<DeleteVPC> deleteVPC;


    @Override
    public void apply() {
        var id = getIdentity();
        var vpcs = outputList(VPCMatch, Vpc.class);
        vpcs.forEach(this::deleteVPC);
    }

    private void deleteVPC(Vpc vpc) {
        var delVpc = create(deleteVPC)
                .withInput(Input.AWS.targetVPCId, vpc.vpcId());
        submit(delVpc);
    }

    @Override
    public Task getDependency() {
        return filterVPCs;
    }

    public void setTargetVPC(String vpcId) {
        filterVPCs.setTargetVPC(vpcId);
    }

}
