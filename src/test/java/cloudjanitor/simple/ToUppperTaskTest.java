package cloudjanitor.simple;

import cloudjanitor.Output;
import cloudjanitor.Tasks;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ToUppperTaskTest {
    @Inject
    Tasks tasks;

    @Inject
    ToUppperTask toUpper;

    @Test
    public void testUpMessage(){
        tasks.runTask(toUpper);
        var message = toUpper.outputString(Output.Sample.UpperMessage);
        assertEquals("HELLO WORLD!", message.get());
    }
}