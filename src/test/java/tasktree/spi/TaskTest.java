package tasktree.spi;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TaskTest {
    @Test
    public void testCaseA(){
        assertEquals(2, 1+1);
    }
}