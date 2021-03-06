package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.route53.model.*;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteRecord extends AWSWrite {

    @Override
    public void apply() {
        var resource = getInput(Input.AWS.ResourceRecordSet, ResourceRecordSet.class);
        log().debug("Deleting record {}", resource);
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
