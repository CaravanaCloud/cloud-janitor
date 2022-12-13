package cj;

import cj.aws.AWSClients;
import cj.aws.AWSClientsManager;
import cj.spi.Task;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;

import javax.inject.Inject;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskTest {
    @Inject
    Logger log;

    @Inject
    protected Tasks tasks;

    @Inject
    protected CJConfiguration config;

    @Inject
    protected AWSClientsManager aws;

    public Logger log() {
        return log;
    }

    @BeforeAll
    public void disableDryRun(){
        //TODO: config.dryRun(false);
    }

    //TODO: Fix tests
    protected AWSClients aws(){
        // var id = DefaultAWSIdentity.of();
        // var region = config.aws().defaultRegion();
        return null; //aws.of(id, region);
    }

    protected CJConfiguration config(){
        return config;
    }

    protected Task submit(Task task){
        return tasks.submitTask(task);
    }
}