package cj.shell;

public record ExecResult(Integer exitCode,
                         String stdout,
                         String stderr) {
    public boolean isSuccess() {
        return exitCode != null && exitCode == 0;
    }
}
