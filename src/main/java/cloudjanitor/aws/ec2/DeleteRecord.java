package cloudjanitor.aws.ec2;

import software.amazon.awssdk.services.route53.model.*;
import cloudjanitor.aws.AWSCleanup;

public class DeleteRecord extends AWSCleanup {
    /*
    public void cleanup(ResourceRecordSet resource) {
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
        aws().newRoute53Client(getRegionOrDefault()).changeResourceRecordSets(request);
    }
    */
}
