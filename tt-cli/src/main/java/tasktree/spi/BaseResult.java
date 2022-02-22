package tasktree.spi;

public record BaseResult<T>(
        String probe,
        Integer exitCode,
        String output,
        Boolean passed,
        Exception exception,
        T value
) {
    private BaseResult() {
        this(null, null, null, null, null, null);
    }

    private static final BaseResult empty = new BaseResult();

    public static final BaseResult empty() {
        return empty;
    }

    public static BaseResult exited(String argsLine, Integer exitCode, String output, Boolean pass) {
        return new BaseResult(argsLine,
                exitCode,
                output,
                pass,
                null,
                null);
    }

    public static BaseResult throwing(String argsLine, Exception e) {
        return new BaseResult(argsLine, null, null, null, e, null);
    }

    public static BaseResult withOutput(String probe, String output) {
        return new BaseResult(
                probe,
                0,
                output,
                null,
                null,
                null
        );
    }

    public static BaseResult success() {
        return BaseResult.empty();
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

