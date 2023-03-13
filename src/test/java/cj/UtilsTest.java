package cj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("unit-test")
@Tag("utillity-class")
@DisplayName("Must be return ")
public class UtilsTest {
	
	@Test
	@DisplayName("string in miliseconds")
	public void testConvertLongNumberToStringMilliSeconds() {
		String expected = "999ms";
		String actual = Utils.msToStr(999L);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("string in seconds")
	public void testConvertLongNumberToStringSeconds() {
		String expected = "1,00s";
		String actual = Utils.msToStr(1_000);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("string in minutes")
	public void testConvertLongNumberToStringMinutes() {
		String expected = "1,00m";
		String actual = Utils.msToStr(60_000);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("string in hours")
	public void testConvertLongNumberToStringHours() {
		String expected = "1,00h";
		String actual = Utils.msToStr(3_600_000);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("string in days")
	public void testConvertLongNumberToStringDays() {
		String expected = "1,00sd";
		String actual = Utils.msToStr(86_400_000);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("the path with files exists")
	public void testReturnSamePathWitFileExists(@TempDir Path tempDir) throws IOException {
		Path expected = Files.createFile(tempDir.resolve("testFileExists.txt"));
		Path actual = Utils.existing(expected);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	@DisplayName("LocalDateTime in string with custom DateTimeFormatter config")
	public void testConvertTodayLocalDateTimeWithCustomFormatter() throws IOException {
		var now = LocalDateTime.now();
		String expected = now.format(Utils.DEFAULT_FMT);
		String actual = Utils.nowStamp();
		Assertions.assertEquals(expected, actual);

	}
}
