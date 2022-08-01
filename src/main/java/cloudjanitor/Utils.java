package cloudjanitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    static final DateTimeFormatter DEFAULT_FMT = DateTimeFormatter.ofPattern("yyyyMMddsshhmmss");

    public static final String nowStamp(){
        var now = LocalDateTime.now();
        return now.format(DEFAULT_FMT);
    }

    public static void main(String[] args) {
        System.out.println(nowStamp());
    }
}
