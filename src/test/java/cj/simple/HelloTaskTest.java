package cj.simple;

import cj.Tasks;
import cj.hello.HelloTask;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static cj.Output.sample.Message;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class HelloTaskTest {
    @Inject
    Tasks tasks;

    @Inject
    HelloTask helloTask;

    @Test
    public void testHello(){
        tasks.submitTask(helloTask);
        var message = helloTask.outputString(Message);
        assertTrue(message.isPresent());
        assertEquals("hello world!", message.get());
    }
}