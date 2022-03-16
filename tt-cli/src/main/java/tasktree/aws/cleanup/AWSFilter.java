package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSResources;
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


    @Override
    public void runSafe() {
        var resourceType = getResourceType();
        var found = filterResources();
        var foundStr = String.join(",", found
                .stream()
                .map(o -> getDescription(o))
                .toList());
        setResourceDescription(foundStr);
        var subtasks = found
                .stream()
                .flatMap(this::mapSubtasks)
                .toList();
        subtasks.forEach(this::addTask);
        log().debug("Filtered {} {} on {}", found.size(), resourceType, getRegion());
    }

    private String getDescription(T t) {
        return AWSResources.getDescription(t);
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

}
