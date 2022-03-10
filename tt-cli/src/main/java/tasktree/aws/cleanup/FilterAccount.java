package tasktree.aws.cleanup;

import software.amazon.awssdk.services.sts.StsClient;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Stream;

@Named("cleanup-aws")
@Dependent
public class FilterAccount extends AWSFilter<String> {

    @Override
    protected List<String> filterResources() {
        var sts = getClient(StsClient.class);
        var accountId = sts.getCallerIdentity().account();
        return List.of(accountId);
    }

    @Override
    protected Stream<Task> mapSubtasks(String accountId) {
        return Stream.of(
                new FilterRegions(),
                new FilterRecords());
    }

    @Override
    protected String getResourceType() {
        return "AWS Account";
    }
}
