package cj.shell;

import cj.ReadTask;
import cj.StreamGobbler;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cj.Input.shell.*;
import static cj.Input.cj.*;
import static cj.Output.shell.*;

@Dependent
@Named("shell")
public class ShellTask extends ReadTask {
    static final Runtime runtime = Runtime.getRuntime();
    static final ExecutorService executor = Executors.newSingleThreadExecutor();
    @Override
    public void apply() {
        var cmdList = inputList(cmds, String.class);
        var cmdArr = (String[]) null;
        if (cmdList.isEmpty()){
            var cmdIn = inputString(cmd);
            if (cmdIn.isPresent()){
                cmdArr = new String[]{cmdIn.get()};
            }
        }else {
            cmdArr = cmdList.toArray(String[]::new);
        }
        if(cmdArr.length > 0) try {
            debug("shell[{}]: {}", cmdArr.length, cmdArr);
            var cmdLine = String.join(" ", cmdArr);
            debug(cmdLine);
            var isDryRun = inputAs(dryRun, Boolean.class).orElse(false);
            if (isDryRun) {
                debug("Dry run, shell command not executed.");
                return;
            }
            var process = runtime.exec(cmdArr);
            var output = new StringBuilder();
            var streamGobbler =
                    new StreamGobbler(process.getInputStream(), output::append);
            var future = executor.submit(streamGobbler);
            var processExitCode = process.waitFor();
            var processOutput = output.toString().trim();
            future.get();
            debug("[{}]$ {}\n{}", processExitCode, cmdLine, processOutput);
            success(stdout, processOutput);
            success(exitCode, processExitCode);
        } catch (Exception e) {
            throw fail(e);
        }
        else throw fail("No commands to execute");
    }

    public static Optional<String> execute(String... cmdArr){
        try {
            var process = runtime.exec(cmdArr);
            var output = new StringBuilder();
            var streamGobbler =
                    new StreamGobbler(process.getInputStream(), output::append);
            var future = executor.submit(streamGobbler);
            var processExitCode = process.waitFor();
            if (processExitCode != 0){
                System.out.println("Process failed. exit code: " + processExitCode);
                return Optional.empty();
            }
            var processOutput = output.toString().trim();
            future.get();
            return Optional.ofNullable(processOutput);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
