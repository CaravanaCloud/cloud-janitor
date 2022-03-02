package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

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

}
