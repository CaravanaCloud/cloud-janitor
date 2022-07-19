package cloudjanitor;

import cloudjanitor.aws.AWSClients;
import cloudjanitor.spi.Task;
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
    protected AWSClients aws;

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
        return aws;
    }

    protected Configuration config(){
        return config;
    }

    protected Task submit(Task task){
        return tasks.submit(task);
    }
}