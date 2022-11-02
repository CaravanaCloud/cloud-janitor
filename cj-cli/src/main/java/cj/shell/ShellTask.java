package cj.shell;

import cj.ReadTask;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import static cj.Input.shell.cmd;

@Dependent
@Named("shell")
public class ShellTask extends ReadTask {
    @Override
    public void apply() {
        var cmdIn = getInputString(cmd);
        debug("shell: {}", cmdIn);

    }
}
