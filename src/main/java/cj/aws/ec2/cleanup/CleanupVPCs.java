package cj.aws.ec2.cleanup;

import cj.aws.AWSInput;
import cj.aws.AWSTask;
import cj.aws.ec2.delete.DeleteVPC;
import cj.aws.ec2.filter.FilterVPCs;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.Output.aws.VPCMatch;

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
                .withInput(AWSInput.targetVPCId, vpc.vpcId());
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
