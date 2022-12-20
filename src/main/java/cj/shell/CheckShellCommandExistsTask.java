package cj.shell;

import cj.BaseTask;

import javax.enterprise.context.Dependent;
import java.util.List;

import static cj.shell.ShellInput.prompt;

@Dependent
public class CheckShellCommandExistsTask extends BaseTask {
    @Override
    public void apply() {
        var cmdIn = getInputString(ShellInput.executable);
        var cmdList = List.of("which", cmdIn);
        var shellTask = shell().shellTask(cmdList);
        submit(shellTask, prompt, cmdList);
        var code = shellTask.outputAs(ShellOutput.exitCode, Integer.class);
        if (code.isEmpty() || code.get() != 0) {
            debug("Command {} does not exist", cmdIn);
            throw fail("Command '%s' not found".formatted(cmdIn));
        } else {
            debug("Command '{}' found", cmdIn);
            success();
        }
    }
}
