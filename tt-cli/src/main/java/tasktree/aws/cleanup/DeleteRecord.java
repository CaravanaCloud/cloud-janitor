package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.model.*;
import tasktree.Configuration;
import tasktree.spi.Task;

public class DeleteRecord extends AWSWrite {
    private final ResourceRecordSet record;

    public DeleteRecord(Configuration config, Region region, ResourceRecordSet resourceRecordSet) {
        super(config, region);
        this.record = resourceRecordSet;
    }

    @Override
    public void run() {
        log().info("Deleting record {}", record);
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
        newRoute53Client().changeResourceRecordSets(request);
    }
}
