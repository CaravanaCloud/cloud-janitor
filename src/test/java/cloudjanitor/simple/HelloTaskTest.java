package cloudjanitor.simple;

import cloudjanitor.Output;
import cloudjanitor.Tasks;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class HelloTaskTest {
    @Inject
    Tasks tasks;

    @Inject
    HelloTask helloTask;

    @Test
    public void testHello(){
        tasks.runTask(helloTask);
        var message = helloTask.findString(Output.Sample.Message);
        assertEquals("hello world!", message.get());
    }
}