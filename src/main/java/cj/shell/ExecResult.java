package cj.shell;

public record ExecResult(Integer exitCode,
                         String stdout,
                         String stderr) {
    public static ExecResult fail(Exception ex) {
        return new ExecResult(null, null, ex.getMessage());
    }

    public boolean isSuccess() {
        return exitCode != null && exitCode == 0;
    }

    public boolean isFailure() {
        return !isSuccess();
    }
}
