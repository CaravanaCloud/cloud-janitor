package tasktree.aws.cleanup.ec2;

import software.amazon.awssdk.services.route53.model.*;
import tasktree.aws.AWSDelete;

public class DeleteRecord extends AWSDelete<ResourceRecordSet> {
    DeleteRecord(ResourceRecordSet rrs){
        super(rrs);
    }

    @Override
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
        aws.newRoute53Client(getRegionOrDefault()).changeResourceRecordSets(request);
    }

    @Override
    protected String getResourceType() {
        return "Record";
    }

}
