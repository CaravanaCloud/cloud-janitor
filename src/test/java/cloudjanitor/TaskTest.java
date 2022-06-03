package cloudjanitor;

import cloudjanitor.aws.AWSClients;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import software.amazon.awssdk.regions.Region;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskTest {
    @Inject
    protected AWSClients aws;

    @Inject
    protected Tasks tasks;

    @Inject
    protected Configuration config;

    @BeforeAll
    public void disableDryRun(){
        //TODO: config.dryRun(false);
    }

    protected AWSClients aws(){
        return aws;
    }

    protected Configuration config(){
        return config;
    }
}