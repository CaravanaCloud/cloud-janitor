package cloudjanitor;

import cloudjanitor.aws.AWSClients;
import cloudjanitor.spi.Task;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;

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

    protected Task submit(Task task){
        return tasks.submit(task);
    }
}