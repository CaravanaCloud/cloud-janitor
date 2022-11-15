package cj.aws.ec2.delete;

import cj.Input;
import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.route53.model.*;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteRecord extends AWSWrite {

    @Override
    public void apply() {
        var resource = getInput(AWSInput.resourceRecordSet, ResourceRecordSet.class);
        debug("Deleting record {}", resource);
        var change = Change.builder()
                .resourceRecordSet(resource)
                .action(ChangeAction.DELETE)
                .build();
        var changes = ChangeBatch.builder()
                .changes(change)
                .build();
        var request = ChangeResourceRecordSetsRequest.builder()
                .changeBatch(changes)
                .build();
        aws().route53().changeResourceRecordSets(request);
    }
}
