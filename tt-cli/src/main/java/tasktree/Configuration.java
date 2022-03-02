package tasktree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;
import tasktree.spi.Tasks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;

@ApplicationScoped
public class Configuration {
    static final ObjectMapper mapper = new ObjectMapper();
    private static final int MIN_PREFIX_LENGTH = 4;

    @Inject
    Logger log;

    @Inject
    Tasks tasks;

    @ConfigProperty(name = "tt.task", defaultValue = "help")
    String taskName;

    @ConfigProperty(name = "tt.dryRun", defaultValue = "true")
    boolean dryRun;

    @ConfigProperty(name = "tt.aws.region", defaultValue = "us-east-1")
    String awsRegion;

    @ConfigProperty(name = "tt.aws.cleanup.prefix", defaultValue = "prefix-to-cleanup")
    String awsCleanupPrefix;

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

    public String getAwsCleanupPrefix() {
        return awsCleanupPrefix;
    }    static ObjectWriter writer = createWriter();

    @Override
    public String toString() {
        try {
            var dump = new HashMap<String, String>() {{
                put("task", taskName);
                put("args", String.join(" ",args));
                put("dryRun", "" + dryRun);
                put("aws.region", awsRegion);
                put("aws.cleanup.prefix", awsCleanupPrefix);
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

    public boolean isDryRun() {
        if (unsafeConfig()) {
            log.debug("Enforcing dry run, prefix too short {}", awsCleanupPrefix);
            return true;
        }else
            return dryRun;
    }

    private boolean unsafeConfig() {
        boolean shortPrefix = awsCleanupPrefix == null || (awsCleanupPrefix.length() < MIN_PREFIX_LENGTH);
        if (shortPrefix) {
            return true;
        }
        return false;
    }

    public String getAWSRegion() {
        return awsRegion;
    }

    public boolean filterRegion(String regionName) {
        if (awsRegion == null)
            return true;
        else
            return regionName.equals(awsRegion);
    }

    public Region getRegion() {
        return Region.of(awsRegion);
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
        log.info(toString());
        waitBeforeRun(10 * (1+waitBeforeRun));
    }



}
