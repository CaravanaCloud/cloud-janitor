package cj.aws;

import cj.CJInput;
import cj.aws.filter.FilterRegions;
import cj.spi.Task;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Dependent
public class AWSIteratorTask extends AWSTask {
    @Inject
    AWSClientsManager aws;

    @Inject
    FilterRegions filterRegions;

    @Override
    public void apply() {
        var regionAction = inputAs(CJInput.regionTask, Instance.class);
        var idAction = inputAs(CJInput.identityTask, Instance.class);
        var ids = aws.identities();
        var regions = submit(filterRegions)
                .outputList(AWSOutput.RegionMatches, Region.class);
        for (var id: ids){
            triggerIdentityAction(idAction, id, regions);
            for (var region: regions){
                triggerRegionAction(regionAction, id, region);
            }
        }
    }

    private void triggerIdentityAction(Optional<Instance> idAction, AWSIdentity id, List<Region> regions) {
        if (idAction.isEmpty()) return;
        var obj = idAction.get().get();
        if (obj instanceof Task task){
            debug("Submiting task {} for {} in {}", task, id, regions);
            submit(task
                    .withInput(AWSInput.regions, regions)
                    .withInput(AWSInput.identity, id)
                    .withInput(AWSInput.accountId, id.accountId()));
        } else throw fail("Unsupported instance type: {}", obj.getClass());
    }

    private void triggerRegionAction(Optional<Instance> regionAction, AWSIdentity id, Region region) {
        if (regionAction.isEmpty()) return;
        var obj = regionAction.get().get();
        if (obj instanceof Task task){
            debug("Submiting task {} for {} in {}", task, id, region);
            submit(task
                    .withInput(AWSInput.targetRegion, region)
                    .withInput(AWSInput.identity, id));
        } else throw fail("Unsupported instance type: {}", obj.getClass());
    }
}
