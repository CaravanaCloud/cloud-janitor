package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

import java.util.List;

public abstract class AWSDelete<T> extends AWSWrite<T> {
    public AWSDelete(T resource){
        setResources(List.of(resource));;
    }

    @Override
    public final void runSafe() {
        resources.forEach(this::cleanup);
        success();
    }

    protected void cleanup(T resource) {
        throw new UnsupportedOperationException("cleanup not implemented");
    }


}