package tasktree.aws.cleanup;

import tasktree.aws.AWSTask;

public class AWSWrite<T> extends AWSTask<T> {
    @Override
    public boolean isWrite() {
        return true;
    }
}
