package cj;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

public class StringUtils {
    public static String join(String... args) {
        return String.join(" ", args);
    }

    public static String join(List<String> argsList) {
        return String.join(" ", argsList);
    }

    protected String join(String separator, String... context) {
        return String.join(separator, context);
    }


    static final PrettyTimeParser parser = new PrettyTimeParser();

    static final Pattern NOSEP_REGEX = Pattern.compile(  "\\d{14}");
    static final DateTimeFormatter NOSEP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static Optional<LocalDateTime> parseLocalDateTime(String line) {
        var result = tryParse(line, NOSEP_REGEX, NOSEP_FORMAT);
        return result;
    }

    private static Optional<LocalDateTime> tryParse(String line, Pattern regex, DateTimeFormatter format) {
        var matcher = regex.matcher(line);
        if (matcher.find()) {
            var match = matcher.group(0);
            var ldt = LocalDateTime.parse(match, format);
            return Optional.of(ldt);
        }
        return Optional.empty();
    }


    public static void main(String[] args) {
        System.out.println(parseLocalDateTime("log-bundle-20230208181716-error-tls-timeout"));

        System.out.println(parseLocalDateTime("log-bundle-d-0-8-1-810-1asdf0-error-tls-timeout"));
    }


}
