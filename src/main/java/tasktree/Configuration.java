package tasktree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import tasktree.spi.Task;
import tasktree.spi.Tasks;
import tasktree.visitor.PrintTreeVisitor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;

@ApplicationScoped
public class Configuration {
    static final ObjectMapper mapper = new ObjectMapper();

    @Inject
    Logger log;

    @Inject
    Tasks tasks;

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
        waitBeforeRun(10 * (1+waitBeforeRun));
    }


    public boolean isDryRun() {
        return dryRun;
    }

    public void runTask(Task task) {
        waitBeforeRun();
        if (task.isWrite()
                && isDryRun()) {
            log.info("Dry run: {}", task);
        }else {
            try {
                task.runSafe();
                log.info("Executed {} ({})", task,
                        task.isWrite() ? "W" : "R");
            } catch (Exception e) {
                log.error("Error executing {}: {}", task, e.getMessage());
            }
        }
    }


}
