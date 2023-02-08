package cj.logs;

import cj.OS;
import cj.aws.AWSTask;
import cj.fs.TaskFiles;
import jdk.jfr.Name;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Dependent
@Named("logs-put")
public class LogsPutTask extends AWSTask {
    @Inject
    TaskFiles files;

    @Override
    public void apply() {
        info("Putting logs");
        var username = OS.username();
        var cwd = files.cwd();
        var logGroup = "/%s/%s".formatted(username, cwd);
        var logs = (List<File>) files.findLogFiles();
        try (var cw = aws().cloudwatch()) {
            logs.forEach(log -> putLog(cw, logGroup, log));
        }
    }

    private void putLog(CloudWatchClient cw, String logGroup, File log) {
        debug("Putting log [{}] to group [{}]", log.getName(), logGroup);

    }
}
