package tasktree.aws.cleanup;

import software.amazon.awssdk.regions.Region;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

import java.util.List;

public abstract class AWSDelete<T> extends AWSTask<T> {
    public AWSDelete() {}
    public AWSDelete(Configuration config)  {
        super(config);
    }
    public AWSDelete(Configuration config, Region region)  {
        super(config, region);
    }


    public AWSDelete(T resource){
        setResources(List.of(resource));;
    }

    @Override
    public boolean isWrite() {
        return true;
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