package cj.logs;

import cj.OS;
import cj.aws.AWSTask;
import cj.fs.TaskFiles;
import cj.hello.HelloConfiguration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;

@Dependent
@Named("logs-put")
public class LogsPutTask extends AWSTask {
    @Inject
    TaskFiles files;

    @Override
    public void apply() {
        info("Putting logs");
        var username = OS.username();
        var dataDir = files.dataDir();
        debug("Collecting logs from [{}]", dataDir);
        var dataDirName = dataDir.getFileName().toString();
        var logGroup = "/%s/%s".formatted(username, dataDirName);
        var logs =  files.findLogFiles();
        try (var cw = aws().cloudwatchlogs()) {
            checkLogGroup(cw, logGroup);
            logs.forEach(log -> putLog(cw, logGroup, dataDir, log));
        }
    }

    private void checkLogGroup(CloudWatchLogsClient cw, String logGroup) {
        if (! logGroupExists(cw, logGroup)){
            debug("Creating log group [{}]", logGroup);
            createLogGroup(cw, logGroup);
        }
        debug("Log group [{}] checked", logGroup);
    }

    private void createLogGroup(CloudWatchLogsClient cw, String logGroup) {
        var request = CreateLogGroupRequest.builder()
                .logGroupName(logGroup)
                .build();
        cw.createLogGroup(request);
    }

    private boolean logGroupExists(CloudWatchLogsClient cw, String logGroup) {
        var request = DescribeLogGroupsRequest.builder()
                .logGroupNamePrefix(logGroup)
                .build();
        var response = cw.describeLogGroups(request);
        var groups = response.logGroups();
        var result = groups.stream().anyMatch(g -> g.logGroupName().equals(logGroup));
        return result;
    }

    private void putLog(CloudWatchLogsClient cw, String logGroup, Path dataDir, Path logFile) {
        try {
            var logStream = dataDir.relativize(logFile).toString();
            checkLogStream(cw, logGroup, logStream);
            debug("Putting log [{}] to group [{}]", logStream, logGroup);
            cloudwatchPutLog(cw, logGroup, logStream, logFile);
        }catch (Exception e){
            error("Failed to put log [{}] to group [{}]", logFile, logGroup);
            error(e.getMessage());
        }
    }

    private void checkLogStream(CloudWatchLogsClient cw, String logGroup, String logStream) {
        var exists = logStreamExists(cw, logGroup, logStream);
        if (! exists){
            createLogStream(cw, logGroup, logStream);
        }
        debug("Log stream [{}] checked", logStream);
    }

    private void createLogStream(CloudWatchLogsClient cw, String logGroup, String logStream) {
        debug("Creating log stream [{}]", logStream);
        var request = CreateLogStreamRequest.builder()
                .logGroupName(logGroup)
                .logStreamName(logStream)
                .build();
        cw.createLogStream(request);
        debug("Created log stream [{}]", logStream);
    }

    private boolean logStreamExists(CloudWatchLogsClient cw, String logGroup, String logStream) {
        var request = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroup)
                .logStreamNamePrefix(logStream)
                .build();
        var response = cw.describeLogStreams(request);
        var streams = response.logStreams();
        var result = streams.stream().anyMatch(s -> s.logStreamName().equals(logStream));
        return result;
    }

    private void cloudwatchPutLog(CloudWatchLogsClient cw, String logGroup, String logStream, Path logFile) {
        var events = eventsFromFile(logFile);
        if (events.length > 0){
            var request = PutLogEventsRequest.builder()
                    .logGroupName(logGroup)
                    .logStreamName(logStream)
                    .logEvents(events)
                    .build();
            cw.putLogEvents(request);
            debug("[{}] log events put to [{}://{}]", events.length, logGroup, logStream);
        }else{
            debug("[-] log events to put to [{}://{}]", logGroup, logStream);
        }

    }

    //TODO: set correct timestamp for untimed log messages instead of filtering out
    private InputLogEvent[] eventsFromFile(Path logFile) {
        var lines = files.readLines(logFile);
        var events = lines.stream()
                .map(line -> eventOf(line, logFile))
                .filter(event -> event != null)
                .toArray(InputLogEvent[]::new);
        return events;
    }

    private InputLogEvent eventOf(String line, Path logFile) {
        var time = timestampOf(line, logFile.getFileName());
        if (time != null) {
            return InputLogEvent.builder()
                    //TODO: Set correct timestamp
                    .timestamp(time)
                    .message(line)
                    .build();
        }
        return null;
    }

    private Long timestampOf(String line, Path fileName) {
        //TODO: Map file names to timestamps
        return System.currentTimeMillis();
    }

}
