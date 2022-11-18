package cj.shell;

import cj.Output;
import cj.SafeTask;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

import static cj.shell.ShellInput.cmds;

@Dependent
public class CheckShellCommandExistsTask extends SafeTask {
    @Inject
    Instance<ShellTask> shellTaskInstance;

    @Override
    public void apply() {
        var cmdIn = getInputString(ShellInput.cmd);
        var shellTask = shellTaskInstance.get();
        var cmdList = List.of("which", cmdIn);
        submit(shellTask, cmds, cmdList);
        var code = shellTask.outputAs(Output.shell.exitCode, Integer.class);
        if (code.isEmpty() || code.get() != 0) {
            debug("Command {} does not exist", cmdIn);
            throw fail("Command '%s' not found".formatted(cmdIn));
        }else {
            debug("Command '{}' found", cmdIn);
            success();
        }
    }
}
