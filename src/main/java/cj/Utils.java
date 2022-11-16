package cj;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    static final DateTimeFormatter DEFAULT_FMT = DateTimeFormatter.ofPattern("yyyyMMddsshhmmss");

    public static String nowStamp(){
        var now = LocalDateTime.now();
        return now.format(DEFAULT_FMT);
    }

    static final double SECOND = 1000.00;
    static final double MINUTE = 60.00;
    static final double HOUR = 60.00;
    static final double DAY = 24.00;

    public static String msToStr(long ms){
        if (ms < 1000){
            return ms + "ms";
        } else if (ms < SECOND * MINUTE){
                return "%.2fs".formatted(ms / SECOND);
            } else if (ms < SECOND * MINUTE * HOUR){
                return "%.2fm".formatted(ms / SECOND / MINUTE);
            } else if (ms < SECOND * MINUTE * HOUR * DAY){
                return "%.2fh".formatted(ms / SECOND / MINUTE / HOUR);
            } else {
                return "%.2fsd".formatted(ms / SECOND / MINUTE / HOUR / DAY);
            }
    }

    public static Path existing(Path path){
        if (! path.toFile().exists()){
            path.toFile().mkdirs();
        }
        return path;
    }


}
