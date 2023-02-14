package cj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

public class TimeUtils {
    static final Logger log = LoggerFactory.getLogger(TimeUtils.class);

    static final Pattern NOSEP_REGEX = Pattern.compile(  "\\d{14}");
    static final Pattern TIME0_REGEX = Pattern.compile(  "\\d{2}:\\d{2}:\\d{2}\\.\\d{6}");

    static final DateTimeFormatter NOSEP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    static final DateTimeFormatter TIME0_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");

    public static Optional<LocalDateTime> parseLocalDateTime(String line) {
        return parseLocalDateTime(line, LocalDate.now());
    }
    public static Optional<LocalDateTime> parseLocalDateTime(String line, LocalDate baseDay) {
        var result = tryParseDateTime(line, NOSEP_REGEX, NOSEP_FORMAT);
        if (result.isEmpty()){
            result = tryParseTime(line, baseDay, TIME0_REGEX, TIME0_FORMAT);
        }
        return result;
    }

    private static Optional<LocalDateTime> tryParseTime(String line, LocalDate baseDay, Pattern regex, DateTimeFormatter format) {
        var matcher = regex.matcher(line);
        if (matcher.find()) {
            var match = matcher.group(0);
            var lt = LocalTime.parse(match, format);
            var ldt = LocalDateTime.of(baseDay, lt);
            return Optional.of(ldt);
        }
        return Optional.empty();
    }

    private static Optional<LocalDateTime> tryParseDateTime(String line, Pattern regex, DateTimeFormatter format) {
        var matcher = regex.matcher(line);
        if (matcher.find()) {
            var match = matcher.group(0);
            try{
                var ldt = LocalDateTime.parse(match, format);
                return Optional.of(ldt);
            }catch(DateTimeParseException e){
                log.trace("Failed to parse [{}] as LocalDateTime", match);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }


    public static void main(String[] args) {
        System.out.println(parseLocalDateTime("log-bundle-20230208181716-error-tls-timeout"));

        System.out.println(parseLocalDateTime("log-bundle-d-0-8-1-810-1asdf0-error-tls-timeout"));
    }

    public static Long toTimestamp(LocalDateTime ldt) {
        return ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static LocalDateTime atStartOfDay(LocalDate baseDay) {
        return baseDay.atStartOfDay();
    }
}
