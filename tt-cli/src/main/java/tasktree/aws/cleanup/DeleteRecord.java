package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.model.*;
import tasktree.Configuration;

public class DeleteRecord extends AWSDelete {
    private final ResourceRecordSet record;

    public DeleteRecord(Configuration config, Region region, ResourceRecordSet resourceRecordSet) {
        super(config, region);
        this.record = resourceRecordSet;
    }

    @Override
    public void run() {
        log().debug("Deleting record {}", record);
        var change = Change.builder()
                .resourceRecordSet(record)
                .action(ChangeAction.DELETE)
                .build();
        var changes = ChangeBatch.builder()
                .changes(change)
                .build();
        var request = ChangeResourceRecordSetsRequest.builder()
                .changeBatch(changes)
                .build();
        aws.newRoute53Client(getRegion()).changeResourceRecordSets(request);
    }

    @Override
    public String toString() {
        return super.toString("Record",record.name());
    }
}
