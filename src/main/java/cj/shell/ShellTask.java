package cj.shell;

import cj.ReadTask;
import cj.StreamGobbler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static cj.CJInput.dryRun;
import static cj.Output.shell.exitCode;
import static cj.Output.shell.stdout;
import static cj.shell.ShellInput.*;

@Dependent
@Named("shell")
public class ShellTask extends ReadTask {
    @Inject
    Runtime runtime;
    @Inject
    ExecutorService executor;


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
        if(cmdArr != null && cmdArr.length > 0) try {
            var cmdLine = String.join(" ", cmdArr);
            debug(cmdLine);
            var isDryRun = inputAs(dryRun, Boolean.class).orElse(false);
            if (isDryRun) {
                info("Dry run, shell command not executed.");
                return;
            }
            var timeoutIn = inputAs(timeout, Long.class).orElse(DEFAULT_TIMEOUT_MINS);
            var process = runtime.exec(cmdArr);
            var output = new StringBuffer();
            var error = new StringBuffer();
            var outGobbler = StreamGobbler.of(
                    process.getInputStream(),
                    s -> this.printAndAppend(output, s));
            var errGobbler = new StreamGobbler(process.getErrorStream(),
                    s -> this.printAndAppend(error, s));
            var futureOut = executor.submit(outGobbler);
            var futureErr = executor.submit(errGobbler);
            debug("Waiting up to [{}] minutes for shell command to complete.", timeoutIn);
            futureOut.get(timeoutIn, TimeUnit.MINUTES);
            futureErr.get(timeoutIn, TimeUnit.MINUTES);

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
        s = redact(s);
        output.append(s);
        output.append("\n");
        debug(s);
    }

    private String redact(String s) {
        s = redactRedundantLogLevel(s);
        //TODO: Redact secrets
        return s;
    }

    static final String redundantLogLevelRegex = "level=([\\S]+) msg= [\\s?]";
    private String redactRedundantLogLevel(String s) {
        return s.replaceAll(redundantLogLevelRegex,"");
    }

    public static Optional<String> execute(String... cmdArr){
        try {
            var process = Runtime.getRuntime().exec(cmdArr);
            var output = new StringBuilder();
            var streamGobbler =
                    new StreamGobbler(process.getInputStream(), output::append);
            var future = Executors.newWorkStealingPool().submit(streamGobbler);
            var processExitCode = process.waitFor();
            if (processExitCode != 0){
                System.out.println("Process failed. exit code: " + processExitCode);
                return Optional.empty();
            }
            var processOutput = output.toString().trim();
            future.get();
            return Optional.of(processOutput);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
