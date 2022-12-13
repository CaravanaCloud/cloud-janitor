package cj;

import java.util.List;

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
