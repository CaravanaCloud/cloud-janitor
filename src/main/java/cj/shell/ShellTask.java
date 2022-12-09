package cj.shell;

import cj.BaseTask;
import cj.StreamGobbler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static cj.CJInput.dryRun;
import static cj.shell.ShellOutput.*;
import static cj.shell.ShellInput.*;

@Dependent
@Named("shell")
public class ShellTask extends BaseTask {
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
            var timeoutIn = inputAs(timeout, Long.class)
                    .orElse(config().execTimeout());
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
            trace("[{}]$ {}\n{}\n{}", processExitCode, cmdLine, processOutput, processError);
            success(exitCode, processExitCode);
            success(stdout, processOutput);
            success(stderr, processError);

        } catch (Exception e) {
            throw fail(e);
        }
        else throw fail("No commands to execute");
    }

    private void printAndAppend(StringBuffer output, String s) {
        s = redact(s);
        output.append(s);
        output.append("\n");
        log(s);
    }



    private String redact(String line) {
        var original = ""+line;
        line = redactRedundantLogLevel(line);
        line = redactSecrets(line);
        line = redactExports(line);
        line = redactProfanity(line);
        var redacted = ! original.equals(line);
        var flag = redacted ? "?" : " ";
        return "[%s] %s".formatted(flag, line);
    }

    private String redactProfanity(String line) {
        line = line.replaceAll("[fF]uck", "f***");
        return line;
    }

    private String redactExports(String s) {
        if (s.contains("export")) {
            debug("Export readacted");
            debug(s);
        }
        return s;
    }

    private String redactSecrets(String s) {
        if (s.contains("password")) {
            s = s.replaceAll(".*", "*");
        }
        return s;
    }

    static final String redundantLogLevelRegex = "level=(\\S+) msg=";
    private String redactRedundantLogLevel(String s) {
        return s.replaceAll(redundantLogLevelRegex,"");
    }

    private void log(String msg, Object... args) {
        switch (config().consoleLevel().toLowerCase()){
            case "trace" -> trace(msg, args);
            case "debug" -> debug(msg, args);
            case "info" -> info(msg, args);
            case "warn" -> warn(msg, args);
            case "error" -> error(msg, args);
        }
    }
}
