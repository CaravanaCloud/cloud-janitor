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





}
