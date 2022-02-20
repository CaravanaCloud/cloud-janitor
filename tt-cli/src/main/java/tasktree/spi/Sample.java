package tasktree.spi;

public record Sample(
        String probe,
        Integer exitCode,
        String output,
        Boolean passed,
        Exception exception
) {
    private Sample() {
        this(null, null, null, null, null);
    }

    private static final Sample empty = new Sample();

    public static final Sample empty() {
        return empty;
    }

    public static Sample exited(String argsLine, Integer exitCode, String output, Boolean pass) {
        return new Sample(argsLine,
                exitCode,
                output,
                pass,
                null);
    }

    public static Sample throwing(String argsLine, Exception e) {
        return new Sample(argsLine, null, null, null, e);
    }

    public static Sample withOutput(String probe, String output) {
        return new Sample(
                probe,
                0,
                output,
                null,
                null
        );
    }

    private String trunc(String s) {
        if (s == null) return "";
        int n = 50;
        if (s.length() < n) return s;
        return s.substring(0, Math.min(s.length(), n));
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        if (passed() != null && passed()) {
            out.append("\uD83D\uDFE2 [PASS " + exitCode() + "] " + probe() + " : " + trunc(output()));
        } else if (passed() != null && ! passed()) {
            out.append("\uD83D\uDD34 [FAIL " + exitCode() + "] " + probe() + " : " + trunc(output()));
        } else {
            out.append("\uD83D\uDD34 [NOOP " + exitCode() + "] " + probe() + " : " + trunc(output()));
        }
        return out.toString();
    }
}

