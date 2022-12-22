package cj;

import cj.fs.TaskFiles;
import cj.shell.*;
import cj.spi.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

@ApplicationScoped
public class Shell {
    @Inject
    CJConfiguration config;

    @Inject
    Tasks tasks;

    @Inject
    Instance<ShellTask> shellInstance;

    @Inject
    Instance<RetryTask> retry;

    Map<String, Map<OS, String[]>> installMap = new HashMap<>();

    @Deprecated
    //Use configuration packages instead
    public void mapInstall(String binary, Map<OS, String[]> commands) {
        installMap.put(binary, commands);
    }

    public ExecResult exec(String... cmdArgs) {
        return exec(false, cmdArgs);
    }

    @SuppressWarnings("unused")
    protected ExecResult exec(Boolean dryRun, String... cmdArgs) {
        return exec(config.execTimeout(), dryRun, cmdArgs);
    }

    @SuppressWarnings("all")
    public ExecResult exec(Long timeout, String... cmdArgs) {
        return exec(timeout, false, cmdArgs);
    }

    public ExecResult exec(Long timeoutMins, Boolean isDryRun, String... cmdArgs) {
        if (cmdArgs.length == 1) {
            var cmd = cmdArgs[0];
            if (cmd.contains(" ")) {
                cmdArgs = cmd.split(" ");
            }
        }
        if (timeoutMins == null) {
            timeoutMins = config.execTimeout();
        }
        var shellTask = shellTask(cmdArgs)
                .withInput(ShellInput.timeout, timeoutMins);
        tasks.submitTask(shellTask);
        @SuppressWarnings("all")
        var stdout = shellTask.outputString(ShellOutput.stdout);
        checkArgument(stdout.isPresent(), "No stdout from shell task");
        var stderr = shellTask.outputString(ShellOutput.stderr);
        checkArgument(stderr.isPresent(), "No stderr from shell task");
        var exitCode = shellTask.outputAs(ShellOutput.exitCode, Integer.class);
        checkArgument(exitCode.isPresent(), "No exit code from shell task");
        return new ExecResult(exitCode.get(), stdout.get(), stderr.get());
    }

    public ShellTask shellTask(List<String> prompt) {
        var shellTask = shellInstance.get();
        shellTask.withInput(ShellInput.prompt, prompt);
        return shellTask;
    }

    public ShellTask shellTask(String... prompt) {
        return shellTask(List.of(prompt));
    }

    @Inject
    Instance<CheckShellCommandExistsTask> checkCmd;

    @SuppressWarnings("UnusedReturnValue")
    public Task checkCmd(String executable, Map<OS, String[]> fixMap) {
        var checkTask = checkCmd.get().withInput(ShellInput.executable, executable);
        var installTask = shellTask(OS.get(fixMap));
        return retry(checkTask, installTask);
    }

    public Task retry(Task theMainTask, Task theFixTask) {
        var retryTask = retry.get()
                .withInput(CJInput.task, theMainTask)
                .withInput(CJInput.fixTask, theFixTask);
        return tasks.submitTask(retryTask);
    }

    public void shell(String[] cmd) {
        checkArgument(cmd.length > 0, "No command provided");
        var binary = cmd[0];
        checkCmd(binary, installMap.get(binary));
        exec(cmd);
    }

    public Path taskFile(Task task, String fileName) {
        return taskFile(task.getPathName(), fileName);
    }

    public Path taskFile(String taskName, String fileName) {
        return TaskFiles.taskDir(taskName).resolve(fileName);
    }
}
