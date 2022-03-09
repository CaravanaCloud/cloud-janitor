package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public abstract class AWSFilter<T> extends AWSTask {
    public AWSFilter() {}
    public AWSFilter(Configuration config)  {
        super(config);
    }
    public AWSFilter(Configuration config, Region region)  {
        super(config, region);
    }

    @Override
    public boolean isWrite() {
        return false;
    }

    public String toString(String resourceType,
                           String... obs) {
        return super.asString(
                "Filter",
                resourceType,
                obs);
    }

    @Override
    public void run() {
        var resourceType = getResourceType();
        var found = filterResources();
        var subtasks = found
                .stream()
                .flatMap( r -> mapSubtasks(r))
                .toList();
        subtasks.forEach(this::addTask);
        log().debug("Filtered {} {} on {}", found.size(), resourceType, getRegion());
    }

    protected Stream<Task> mapSubtasks(T t) {
        return Stream.of();
    }

    protected List<T> filterResources() {
        return (List<T>) List.of(new Object());
    }

    @Override
    public String toString() {
        return toString(getResourceType());
    }
}
