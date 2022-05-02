package cloudjanitor.simple;

import cloudjanitor.Tasks;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class HelloTaskTest {
    @Inject
    Tasks tasks;

    @Inject
    HelloTask helloTask;

    @Test
    public void testHello(){
        tasks.runTask(helloTask);
        var message = helloTask.findString("message");
        assertEquals("hello world", message.get());
    }
}