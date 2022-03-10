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
        var foundStr = found.stream().map(this::toString).toList();
        var subtasks = found
                .stream()
                .flatMap(this::mapSubtasks)
                .toList();
        subtasks.forEach(this::addTask);
        log().info("Filtered {} {} on {}: {}", found.size(), resourceType, getRegion(), foundStr);
    }

    protected String toString(T t){
        return t.toString();
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
