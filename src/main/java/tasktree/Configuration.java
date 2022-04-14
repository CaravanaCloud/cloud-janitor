package tasktree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import tasktree.aws.AWSClients;
import tasktree.spi.Task;
import tasktree.spi.Tasks;
import tasktree.visitor.PrintTreeVisitor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;

@ApplicationScoped
public class Configuration {
    static final ObjectMapper mapper = new ObjectMapper();

    @Inject
    Logger log;

    @Inject
    Tasks tasks;

    @Inject
    RateLimiter rateLimiter;

    @ConfigProperty(name = "tt.task", defaultValue = "marvin")
    String taskName;

    @ConfigProperty(name = "tt.dryRun", defaultValue = "true")
    boolean dryRun;

    @ConfigProperty(name="tt.waitBeforeRun", defaultValue = "1000")
    long waitBeforeRun;

    String[] args;

    private static synchronized ObjectWriter createWriter() {
        if (writer != null) return writer;
        //TODO: Filter out properties with $
        writer = mapper.writer().withDefaultPrettyPrinter();
        return writer;
    }


    public String getTaskName() {
        return taskName;
    }


    static ObjectWriter writer = createWriter();

    @Override
    public String toString() {
        try {
            var dump = new HashMap<String, String>() {{
                put("task", taskName);
                put("args", String.join(" ",args));
                put("dryRun", "" + dryRun);
            }};
            return writer.writeValueAsString(dump);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "Configuration{?}";
    }

    public void parse(String[] args) {
        this.args = args;
        if (args.length > 0) {
            taskName = args[0];
        }
    }


    public Tasks getTasks() {
        return tasks;
    }

    public void waitBeforeRun() {
        waitBeforeRun(null);
    }

    public void waitBeforeRun(Long wait) {
        var sleep = wait == null ? waitBeforeRun : wait;
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init(String[] args) {
        parse(args);
        log.info("TaskTree Configuration: {}", this);
        waitBeforeRun();
    }


    public boolean isDryRun() {
        return dryRun;
    }

    public Result runTask(Task task) {
        waitBeforeRun();
        var startTime = LocalDateTime.now();
        var result = (Result) null;
        if (task.isWrite()
                && isDryRun()) {
            result = Result.dryRun(task);
            log.info("Dry run: {}", task);
        }else {
            try {
                //TODO: Consider not re-executing task
                task.runSafe();
                result = task.getResult();
                if (result == null){
                    result = Result.success(task);
                }
                task.debug("Executed {} ({})",
                        task.toString(),
                        task.isWrite() ? "W" : "R");
                //TODO: General waiter
                if(task.isWrite()) {
                    rateLimiter.waitAfterTask(task);
                }
            } catch (Exception e) {
                result = Result.failure(task, e);
                task.error("Error executing {}: {}", task.toString(), e.getMessage());
            }
        }

        var endTime = LocalDateTime.now();
        result.setEndTime(endTime);
        task.setResult(result);
        return result;
    }

    public AWSClients aws() {
        return AWSClients.getInstance();
    }
}
