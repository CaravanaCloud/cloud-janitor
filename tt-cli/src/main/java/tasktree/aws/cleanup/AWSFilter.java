package tasktree.aws.cleanup;

import tasktree.Configuration;
import tasktree.aws.AWSTask;

public abstract class AWSFilter<T> extends AWSTask {
    public AWSFilter() {}
    public AWSFilter(Configuration config)  {
        super(config);
    }
}
