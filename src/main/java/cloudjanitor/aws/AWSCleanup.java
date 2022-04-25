package cloudjanitor.aws;

import java.util.List;
import java.util.Optional;

public abstract class AWSCleanup<T> extends AWSWrite<T> {
    public AWSCleanup(){}

    public AWSCleanup(T resource){
        setResources(List.of(resource));
    }

    @Override
    public final void runSafe() {
        resources.forEach(this::cleanup);

        success();
    }

    protected void cleanup(T resource) {
        throw new UnsupportedOperationException("cleanup not implemented");
    }


    @Override
    public Optional<Long> getWaitAfterRun() {
        return Optional.of(5_000L);
    }


}