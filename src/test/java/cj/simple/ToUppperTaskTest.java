package cj.simple;

import cj.Output;
import cj.Tasks;
import cj.hello.ToUppperTask;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ToUppperTaskTest {
    @Inject
    Tasks tasks;

    @Inject
    ToUppperTask toUpper;

    @Test
    public void testUpMessage(){
        tasks.submitTask(toUpper);
        var message = toUpper.outputString(Output.sample.UpperMessage);
        assertEquals("HELLO WORLD!", message.get());
    }
}