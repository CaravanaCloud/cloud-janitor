package cj.shell;

import cj.*;
import cj.fs.TaskFiles;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static cj.shell.ShellInput.*;
import static cj.shell.ShellOutput.*;

@Dependent
@Named("shell")
public class ShellTask extends BaseTask {
    @Inject
    Runtime runtime;
    @Inject
    ExecutorService executor;

    @Inject
    TaskFiles files;

    @Override
    public void apply() {
        var cmdList = inputList(prompt, String.class);
        if (cmdList.isEmpty()) fail("No command to execute");
        var cmdLine = String.join(" ", cmdList);
        checkpoint(Capabilities.LOCAL_SHELL,"$ {}", cmdLine);
        try{
            safeExec(cmdList);
        } catch (IOException
                 | InterruptedException
                 | ExecutionException
                 | TimeoutException ex) {
            throw fail(ex);
        }
    }

    private ExecResult safeExec(List<String> prompt) throws IOException,
            InterruptedException,
            ExecutionException,
            TimeoutException {
        var taskConfig = configuration()
                .taskConfigForQuery(prompt);
        var install = taskConfig
                .flatMap(TaskConfiguration::install);
        var check = install
                .flatMap(InstallConfig::check);
        check.ifPresent( c -> checkInstall(install.get(), c));
        return exec(prompt);
    }

    private void checkInstall(InstallConfig installConfig, String checkPrompt) {
        var result = runCheck(checkPrompt);
        if (result.isFailure()) {
            debug("Install check failed, installing...");
            runInstall(installConfig);
            debug("Re-checking install...");
            var recheck = runCheck(checkPrompt);
            if (recheck.isFailure()){
                throw fail("Failed to pass task check");
            }else{
                debug("Task install recheck success");
            }
        }else {
            debug("Install check success");
        }
    }

    private void runInstall(InstallConfig installConfig) {
        //TODO: Check OS/Arch and run install
        installPackage(installConfig);
    }

    private void installPackage(InstallConfig installConfig) {
        var pkgCfg = installConfig.packages();
        var pkgUrl = pkgCfg.flatMap(PackagesConfig::byOSandArch);
        debug("Download and install package: {}", pkgUrl);
        pkgUrl.ifPresent(url -> installPkgFromUrl(pkgCfg.get(), url));
    }

    private void installPkgFromUrl(PackagesConfig pkgCfg, String url) {
        var pkgDir = files.packageDir();
        debug("Download {} to {}", url, pkgDir);
        var pkgFile = downloadPkg(url, pkgDir);
        if (pkgFile == null) return;
        var extractDir = extractPkg(pkgFile);
        if (extractDir == null) return;
        linkPath(pkgCfg, extractDir);
        debug("install completed");
    }

    private void linkPath(PackagesConfig pkgCfg, Path extractDir) {
        var binPath = files.firstPathInHome();
        var executables = pkgCfg.executables();
        executables.forEach(e -> linkExecutable(pkgCfg, e, extractDir, binPath) );
    }

    private void linkExecutable(PackagesConfig pkgCfg, ExecutableConfig e, Path extractDir, Path binPath) {
        var exePath = e.path();
        var exeLink = e.link().orElse(exePath);
        var from = extractDir.resolve(exePath);
        var to = binPath.resolve(exeLink);
        if (to.toFile().exists()) {
            debug("Link target {} already exists, skipping", to);
            return;
        }
        debug("Link {} => {}", from, to);
        try {
            Files.createSymbolicLink(to, from);
            Thread.sleep(5000);
        } catch (IOException | InterruptedException ex) {
            warn("Failed to link {} to {}: {}", from, to, ex);
        }
    }

    private Path extractPkg(Path pkgFile) {
        if (pkgFile.toString().endsWith(".tar.gz")){
            return extractTarGz(pkgFile);
        } else {
            warn("Unsupported package format: {}", pkgFile);
        }
        return null;
    }

    private Path extractTarGz(Path pkgFile) {
        var pkgDir = pkgFile.getParent();
        var dirName = baseName(pkgFile);
        var extractDir = TaskFiles.resolveDir(pkgDir, dirName);
        try (var in = new FileInputStream(pkgFile.toFile())) {
            var fin = new TarArchiveInputStream(new GzipCompressorInputStream(in));
            TarArchiveEntry entry = null;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                var file = extractDir.resolve(entry.getName()).toFile();
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                var fout = new FileOutputStream(file);
                IOUtils.copy(fin, fout);
                fout.close();
                trace("file extracted, fixing permissions");
                if (entry.isFile()){
                    var fileMode = entry.getMode();
                    var isExecutable = (fileMode & 0b100) != 0;
                    if (isExecutable)
                        debug("Setting as executable: {}",entry.getName());
                    file.setExecutable(isExecutable);
                }
            }
            debug("Package extracted to {}", extractDir);
            return extractDir;
        } catch (IOException e) {
            warn("Failed to extract tar.gz file: {}", e.getMessage());
        }
        return null;
    }

    private String baseName(Path pkgFile) {
        var fileName = pkgFile.getFileName().toString();
        if (fileName.endsWith(".tar.gz"))
            return fileName.substring(0, fileName.length() - 7);
        else if (fileName.endsWith(".tgz")
            || fileName.endsWith(".zip"))
            return fileName.substring(0, fileName.length() - 4);
        else
            return fileName.substring(0, fileName.indexOf('.'));
    }

    private Path downloadPkg(String url, Path pkgDir)  {
        try (var in = new BufferedInputStream(
                new URL(url).openStream())){
            var fileName = fileNameFromUrl(url);
            var filePath = pkgDir.resolve(fileName).toAbsolutePath();
            if (fileName == null) return filePath;
            var fileOutputStream = new FileOutputStream(filePath.toString());
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            debug("Package downloaded to {}", filePath);
            return filePath;
        } catch (IOException e) {
            error("Failed to download package", e);
            return null;
        }
    }

    private String fileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private ExecResult runCheck(String line) {
        var prompt = List.of(line.split(" "));
        try {
            debug("Running check: {}", prompt);
            return exec(prompt);
        } catch (IOException
                | InterruptedException
                | ExecutionException
                | TimeoutException ex) {
            warn("Check failed: {}", line);
            debug("Exception: {}", ex.getMessage());
            return ExecResult.fail(ex);
        }
    }

    private ExecResult exec(List<String> cmdList)
            throws IOException,
            InterruptedException,
            ExecutionException,
            TimeoutException {
        var cmdArr = cmdList.toArray(String[]::new);
        var output = new StringBuffer();
        var error = new StringBuffer();
        var process = runtime.exec(cmdArr);
        var processIn = process.getInputStream();
        var processErr = process.getErrorStream();
        var outGobbler = StreamGobbler.of(
                processIn,
                s -> this.printAndAppend(output, s));
        var errGobbler = new StreamGobbler(processErr,
                s -> this.printAndAppend(error, s));
        var futureOut = executor.submit(outGobbler);
        var futureErr = executor.submit(errGobbler);
        var timeoutIn = inputAs(timeout, Long.class)
                .orElse(config().execTimeout());
        trace("Waiting up to [{}] minutes for shell command to complete.", timeoutIn);
        var isDone = process.waitFor(timeoutIn, TimeUnit.MINUTES);
        futureOut.get(timeoutIn, TimeUnit.MINUTES);
        futureErr.get(timeoutIn, TimeUnit.MINUTES);
        var processExitCode = process.exitValue();
        var processOutput = output.toString();
        var processError = error.toString();
        var cmdLine = String.join(" ", cmdList);
        trace("[{}]$ {}\n{}\n{}", processExitCode, cmdLine, processOutput, processError);
        success(exitCode, processExitCode);
        success(stdout, processOutput);
        success(stderr, processError);
        var result = new ExecResult(processExitCode, processOutput, processError);
        return success(result);
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
            trace("Export readacted");
            trace(s);
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
