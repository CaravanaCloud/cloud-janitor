package cj.shell;

import cj.Input;

public enum ShellInput implements Input {
    cmd,
    cmds,
    timeout,
    dryRun;

    public static final Long DEFAULT_TIMEOUT_MINS = 1L;
}