package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSFilter;

import javax.enterprise.context.Dependent;

@Dependent
public class FilterRecords extends AWSFilter {
    /*
    @ConfigProperty(name = "tt.ocp.baseDomain", defaultValue = "redhat.com")
    String baseDomain;

    @Override
    public void runSafe() {
        var r53 = aws().newRoute53Client(getRegionOrDefault());
        r53.listHostedZones()
                .hostedZones()
                .stream()
                .filter(this::match)
                .forEach(this::filterRecords);
    }

    private void filterRecords(HostedZone hostedZone) {
        var r53 = aws().newRoute53Client(getRegionOrDefault());
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



    private boolean matchRecord(ResourceRecordSet rrs) {
        var name = rrs.name();
        matchName(name);
    }


    private boolean match(HostedZone zone) {
        if (baseDomain==null) {
            log().trace("base domain not set");
            return false;
        }
        var zoneName = zone.name();
        var match = zoneName.startsWith(baseDomain);
        log().trace("Found Hosted Zone {} {} ", mark(match), zoneName);
        return match;
    }

    @Override
    protected Region getRegionOrDefault() {
        return Region.AWS_GLOBAL;
    }

    @Override
    protected String getResourceType() {
        return "Records";
    }

     */
}
