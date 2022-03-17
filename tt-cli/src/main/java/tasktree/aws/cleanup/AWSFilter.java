package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSResources;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

import java.util.List;
import java.util.stream.Stream;

public abstract class AWSFilter<T> extends AWSTask<T> {
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
        resources = filterResources();
        log().info("Filtered {} {} on  => {}", resources.size(), getResourceType(), getRegion(), getSubtasks().size());
    }

    protected String toString(T t){
        return t.toString();
    }

    protected List<T> filterResources() {
        return List.of();
    }


}
