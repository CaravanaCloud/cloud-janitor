package cloudjanitor.aws;

import software.amazon.awssdk.regions.Region;
import cloudjanitor.Configuration;

import java.util.List;

public abstract class AWSFilter<T> extends AWSTask<T> {
    public AWSFilter() {}
    public AWSFilter(Configuration config)  {
        super(config);
    }
    public AWSFilter(Region region)  {
        super(region);
    }


    @Override
    public boolean isWrite() {
        return false;
    }

    @Override
    public void runSafe() {
        setResources(filterResources());
    }

    protected List<T> filterResources() {
        return List.of();
    }

}