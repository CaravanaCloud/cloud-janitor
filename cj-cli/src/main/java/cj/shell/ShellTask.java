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
            trace("shell[{}]: {}", cmdArr.length, cmdArr);
            var cmdLine = String.join(" ", cmdArr);
            debug(cmdLine);
            var isDryRun = inputAs(dryRun, Boolean.class).orElse(false);
            if (isDryRun) {
                info("Dry run, shell command not executed.");
                return;
            }
            var process = runtime.exec(cmdArr);
            var output = new StringBuffer();
            var error = new StringBuffer();
            var outGobbler =
                    new StreamGobbler(process.getInputStream(),
                            s -> this.printAndAppend(output, s));
            var errGobbler = new StreamGobbler(process.getErrorStream(),
                    s -> this.printAndAppend(error, s));
            var futureOut = executor.submit(outGobbler);
            var futureErr = executor.submit(errGobbler);
            futureOut.get();
            futureErr.get();
            var processExitCode = process.waitFor();
            var processOutput = output.toString().trim();
            var processError = error.toString().trim();
            debug("[{}]$ {}\n{}\n{}", processExitCode, cmdLine, processOutput, processError);
            success(stdout, processOutput);
            success(exitCode, processExitCode);
        } catch (Exception e) {
            throw fail(e);
        }
        else throw fail("No commands to execute");
    }

    private void printAndAppend(StringBuffer output, String s) {
        output.append(s);
        output.append("\n");
        debug(s);
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
