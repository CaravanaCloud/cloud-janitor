package cj;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TimeUtilsTest {
    @Test
    public void parseValidDateTimeNoSeparator(){
        var line = "log-bundle-20230208181716-error-tls-timeout";
        var expected = LocalDateTime.of(
                2023, 2, 8,
                18, 17, 16);
        var actual = TimeUtils.parseLocalDateTime(line);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }
}