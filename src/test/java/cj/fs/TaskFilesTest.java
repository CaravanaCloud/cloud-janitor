package cj.fs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TaskFilesTest {

    @Test
    void testLoadEnvrc() {
        // given
        var lines = List.of("export A=1", "export B=2");
        // when
        var envrc = TaskFiles.parseExports(lines);
        // then
        var a = envrc.get("A");
        var b = envrc.get("B");
        assertEquals("1", a);
        assertEquals("2", b);
    }

    @Test
    void testLoadEnvrcWithComments() {
        // given
        var lines = List.of("export A=1", "# export B=2");
        // when
        var envrc = TaskFiles.parseExports(lines);
        // then
        var a = envrc.get("A");
        var b = envrc.get("B");
        assertEquals("1", a);
        assertNull(b);
    }

    @Test
    void testLoadEnvrcQuotes() {
        // given
        var lines = List.of("export A=\"1\"", "export B=\"2 + 2\"");
        // when
        var envrc = TaskFiles.parseExports(lines);
        // then
        var a = envrc.get("A");
        var b = envrc.get("B");
        assertEquals("1", a);
        assertEquals("2 + 2", b);
    }

    @Test
    void testLoadEnvrcSingleQuotes() {
        // given
        var lines = List.of("export A='2 + 2'", "export B=1");
        // when
        var envrc = TaskFiles.parseExports(lines);
        // then
        var a = envrc.get("A");
        var b = envrc.get("B");
        assertEquals("2 + 2", a);
        assertEquals("1", b);
    }

    @Test
    void testLoadEnvValueSimple(){
        var line = "export A=1";
        var value = TaskFiles.exportValue(line);
        assertEquals(value, "1");
    }

}