package cj.logs;

import cj.OS;
import cj.TimeUtils;
import cj.aws.AWSTask;
import cj.fs.TaskFiles;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Dependent
@Named("logs-put")
public class LogsPutTask extends AWSTask {
    private static final int CWLOGS_MAX_LINE_LENGTH = 10000;
    @Inject
    TaskFiles files;

    @Override
    public void apply() {
        info("Putting logs");
        var username = OS.username();
        var dataDir = files.dataDir();
        var dataDirName = dataDir.getFileName().toString();
        var logGroup = "/%s/%s".formatted(username, dataDirName);
        var logs =  files.findLogFiles();
        debug("Collected [{}] log files from [{}]", logs.size(), dataDirName);

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
            var baseDay = TimeUtils.parseLocalDateTime(logFile.toAbsolutePath().toString())
                    .orElse(LocalDateTime.now())
                    .toLocalDate();
            var logStream = dataDir.relativize(logFile).toString();
            checkLogStream(cw, logGroup, logStream);
            debug("Putting log [{}] to group [{}] with baseDay [{}]", logStream, logGroup, baseDay);
            cloudwatchPutLog(cw, logGroup, logStream, logFile, baseDay);
        }catch (Exception e){
            error("Failed to put log [{}] to group [{}]", logFile, logGroup);
            error(e.getMessage());
            e.printStackTrace();
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

    private void cloudwatchPutLog(CloudWatchLogsClient cw, String logGroup, String logStream, Path logFile, LocalDate baseDay) {
        var lines = linesOf(logFile);
        var events = eventsFromFile(logFile, baseDay);
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

    private List<String> linesOf(Path logFile) {
        var lines = files.readLines(logFile);
        debug("Read [{}] lines from [{}]", lines.size(), logFile);
        return lines;
    }

    //TODO: set correct timestamp for untimed log messages instead of filtering out
    private InputLogEvent[] eventsFromFile(Path logFile, LocalDate baseDay) {
        var lines = files.readLines(logFile);
        var events = new LinkedList<InputLogEvent>();
        var time = TimeUtils.toTimestamp(TimeUtils.atStartOfDay(baseDay));
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            if (line.length() >= CWLOGS_MAX_LINE_LENGTH)
                line = line.substring(0, CWLOGS_MAX_LINE_LENGTH - 1);
            var timestamp = timestampOf(line, logFile, baseDay);
            if (timestamp != null){
                time = timestamp;
            } else {
                timestamp = time;
            }
            var event = createEvent(timestamp, line);
            events.add(event);
        }
        var ascendingOrder = checkAscendingTimeOrder(events);
        if (! ascendingOrder){
            warn("Events are not in ascending order. Resorting...");
            warn("File: [{}]", logFile);
            warn("Events: [{}]", events.size());
            events.sort(Comparator.comparing(InputLogEvent::timestamp));
            ascendingOrder = checkAscendingTimeOrder(events);
            if (! ascendingOrder){
                error("Events are still not in ascending order. Skipping...");
                return new InputLogEvent[0];
            }
        }
        var result = events.toArray(InputLogEvent[]::new);
        return result;
    }

    private boolean checkAscendingTimeOrder(LinkedList<InputLogEvent> events) {
        var last = Long.MIN_VALUE;
        for (InputLogEvent event : events) {
            if (event.timestamp() < last){
                return false;
            }
            last = event.timestamp();
        }
        return true;
    }


    private InputLogEvent createEvent(Long time, String line) {
        return InputLogEvent.builder()
                .timestamp(time)
                .message(line)
                .build();
    }

    private Long timestampOf(String line, Path fileName, LocalDate baseDay) {
        var ldt = TimeUtils.parseLocalDateTime(line, baseDay);
        var timestamp = ldt.map(TimeUtils::toTimestamp).orElse(null);
        return timestamp;
    }

}
