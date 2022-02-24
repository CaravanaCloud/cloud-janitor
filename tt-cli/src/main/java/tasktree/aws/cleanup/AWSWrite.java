package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

public abstract class AWSWrite  extends AWSTask {
    public AWSWrite() {}
    public AWSWrite(Configuration config)  {
        super(config);
    }
    public AWSWrite(Configuration config, Region region)  {
        super(config, region);
    }

    @Override
    public boolean isWrite() {
        return true;
    }
}