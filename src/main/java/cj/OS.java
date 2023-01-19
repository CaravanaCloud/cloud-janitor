package cj;

import java.util.Map;

public enum OS {
    linux,
    mac,
    windows,
    solaris;

    private static OS os;

    public synchronized static OS of() {
        if (os == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                os = OS.windows;
            } else if (osName.contains("nix") || osName.contains("nux")
                    || osName.contains("aix")) {
                os = OS.linux;
            } else if (osName.contains("mac")) {
                os = OS.mac;
            } else if (osName.contains("sunos")) {
                os = OS.solaris;
            }
        }
        return os;
    }

    public static <T> T get(Map<OS, T> taskMap) {
        return taskMap.get(of());
    }

    public static String username() {
        return System.getProperty("user.name");
    }
}
