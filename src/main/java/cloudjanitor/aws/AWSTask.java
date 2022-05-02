package cloudjanitor.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.regions.Region;
import cloudjanitor.BaseTask;
import cloudjanitor.spi.Task;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

public abstract class AWSTask
        extends BaseTask {

    @Inject
    private AWSClients aws;

    public void setRegion(Region region) {
        aws.setRegion(region);
    }

    public AWSClients aws(){
        return aws;
    }


}