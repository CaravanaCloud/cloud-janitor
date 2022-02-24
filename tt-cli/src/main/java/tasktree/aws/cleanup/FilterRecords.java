package tasktree.aws.cleanup;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import tasktree.Configuration;

import javax.enterprise.context.Dependent;
import java.util.function.Consumer;

@Dependent
public class FilterRecords extends AWSFilter<Record> {
    long filtered = 0;
    long matched = 0;

    @ConfigProperty(name = "tt.ocp.baseDomain", defaultValue = "redhat.com")
    String baseDomain;

    private Route53Client r53 = newRoute53Client();


    public FilterRecords(Configuration config) {
        super(config);
    }

    @Override
    public void run() {
        r53.listHostedZones()
                .hostedZones()
                .stream()
                .filter(this::match)
                .forEach(this::filterRecords);
    }

    private void filterRecords(HostedZone hostedZone) {
        var listRecords = ListResourceRecordSetsRequest
                .builder()
                .hostedZoneId(hostedZone.id())
                .build();
        r53.listResourceRecordSetsPaginator(listRecords)
                .resourceRecordSets()
                .stream()
                .filter(this::matchRecord)
                .forEach(this::deleteRecord);
        log().debug("Macthed {}/{} records", matched, filtered);
    }

    private void deleteRecord(ResourceRecordSet resourceRecordSet) {
        addTask(new DeleteRecord(getConfig(), getRegion(), resourceRecordSet));
    }

    private boolean matchRecord(ResourceRecordSet rrs) {
        var name = rrs.name();
        var prefix = getConfig().getAwsCleanupPrefix();
        var match = name.startsWith(prefix);
        if(match) {
            matched++;
            log().debug("Found {} {} {} ","ResourceRecordSet", mark(true), name);
        }
        filtered++;
        return match;
    }


    private boolean match(HostedZone zone) {
        var zoneName = zone.name();
        var match = zoneName.startsWith(baseDomain);
        log().debug("Found Hosted Zone {} {} ", mark(match), zoneName);
        return match;
    }

    @Override
    protected Region getRegion() {
        return Region.AWS_GLOBAL;
    }
}
