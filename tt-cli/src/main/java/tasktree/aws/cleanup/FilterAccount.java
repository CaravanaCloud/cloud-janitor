package tasktree.aws.cleanup;

import org.slf4j.Logger;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Stream;

@Named("cleanup-aws")
@Dependent
public class FilterAccount extends AWSFilter<Object> {
    @Override
    protected Stream<Task> mapSubtasks(Object unused) {
        Stream<Task> subtasks =  Stream.of(new FilterRegions(),
                new FilterRecords());
        return subtasks;
    }

    @Override
    protected String getResourceType() {
        return "AWS Account";
    }
}
