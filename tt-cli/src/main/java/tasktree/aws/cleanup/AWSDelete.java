package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

public abstract class AWSDelete extends AWSTask {
    public AWSDelete() {}
    public AWSDelete(Configuration config)  {
        super(config);
    }
    public AWSDelete(Configuration config, Region region)  {
        super(config, region);
    }

    @Override
    public boolean isWrite() {
        return true;
    }

    public String toString(String resourceType,
                           String... obs) {
        return super.asString(
                "Delete",
                resourceType,
                obs);
    }
}