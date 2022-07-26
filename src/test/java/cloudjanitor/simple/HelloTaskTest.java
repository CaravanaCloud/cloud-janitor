package cloudjanitor.simple;

import cloudjanitor.Output;
import cloudjanitor.Tasks;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static cloudjanitor.Output.Sample.Message;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class HelloTaskTest {
    @Inject
    Tasks tasks;

    @Inject
    HelloTask helloTask;

    @Test
    public void testHello(){
        tasks.submit(helloTask);
        var message = helloTask.outputString(Message);
        assertTrue(message.isPresent());
        assertEquals("hello world!", message.get());
    }
}