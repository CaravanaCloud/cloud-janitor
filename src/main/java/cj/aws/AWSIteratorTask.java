package cj.aws;

import cj.BaseTask;
import cj.CJInput;
import cj.aws.filter.FilterRegions;
import cj.spi.Task;
import com.google.common.base.Preconditions;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static cj.CJInput.task;
import static com.google.common.base.Preconditions.checkArgument;

@Dependent
public class AWSIteratorTask extends AWSTask {
    @Inject
    AWSClientsManager aws;

    @Inject
    FilterRegions filterRegions;

    @Override
    public void apply() {
        var instance = inputAs(CJInput.instance, Instance.class);
        checkArgument(instance.isPresent(), "Instance input is required");
        var ids = aws.identities();
        var regions = submit(filterRegions)
                .outputList(AWSOutput.RegionMatches, Region.class);
        for (var id: ids){
            for (var region: regions){
                var obj = instance.get().get();
                if (obj instanceof Task task){
                    debug("Submiting task {} for {} in {}", task, id, region);
                    submit(task
                            .withInput(AWSInput.targetRegion, region)
                            .withInput(AWSInput.identity, id));
                } else throw fail("Unsupported instance type: {}", obj.getClass());
            }
        }
    }
}
