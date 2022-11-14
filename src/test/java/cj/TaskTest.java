package cj;

import cj.aws.AWSClients;
import cj.aws.DefaultAWSIdentity;
import cj.spi.Task;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;

import javax.inject.Inject;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskTest {
    @Inject
    Logger log;

    @Inject
    protected Tasks tasks;

    @Inject
    protected Configuration config;

    public Logger log() {
        return log;
    }

    @BeforeAll
    public void disableDryRun(){
        //TODO: config.dryRun(false);
    }

    protected AWSClients aws(){
        var id = DefaultAWSIdentity.of();
        var region = config.aws().defaultRegion();
        return AWSClients.of(config.aws(), id);
    }

    protected Configuration config(){
        return config;
    }

    protected Task submit(Task task){
        return tasks.submit(task);
    }
}