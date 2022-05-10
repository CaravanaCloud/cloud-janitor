package cloudjanitor.aws;

import cloudjanitor.LogConstants;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;
import cloudjanitor.BaseTask;
import cloudjanitor.spi.Task;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.*;

public abstract class AWSTask
        extends BaseTask
        implements LogConstants {

    @Inject
    AWSClients aws;

    public void setRegion(Region region) {
        aws.setRegion(region);
    }

    public AWSClients aws(){
        return aws;
    }

    protected <T> T create(Instance<T> instance){
        var result = instance.get();
        if (result instanceof AWSTask awsTask){
            awsTask.setRegion(aws().getRegion());
        }
        return result;
    }

    protected Region getRegion(){
        return aws().getRegion();
    }
}