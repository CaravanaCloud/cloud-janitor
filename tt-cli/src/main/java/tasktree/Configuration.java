package tasktree;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;
import tasktree.spi.Task;
import tasktree.spi.Tasks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class Configuration {
    private static final int MIN_PREFIX_LENGTH = 4;
    @Inject
    Logger log;

    @Inject
    Tasks tasks;

    @ConfigProperty(name = "tt.root", defaultValue = "help")
    String root;

    @ConfigProperty(name = "tt.dryRun", defaultValue = "true")
    boolean dryRun;

    @ConfigProperty(name = "tt.aws.region", defaultValue = "us-east-1")
    String awsRegion;

    @ConfigProperty(name = "tt.aws.cleanup.prefix", defaultValue = "prefix-to-cleanup")
    String awsCleanupPrefix;

    public String getRoot() {
        return root;
    }

    public String getAwsCleanupPrefix() {
        return awsCleanupPrefix;
    }

    static final ObjectMapper mapper = new ObjectMapper();
    static ObjectWriter writer = createWriter();

    private static synchronized ObjectWriter createWriter() {
        if (writer != null) return writer;
        //TODO: Filter out properties with $
        writer = mapper.writer().withDefaultPrettyPrinter();
        return writer;
    }

    @Override
    public String toString() {
        try {
            var dump = new HashMap<String, String>() {{
                put("root", root);
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
        log.info("Args: %s".formatted(Arrays.toString(args)));
        if (args.length > 0) {
            root = args[0];
        }
    }

    public boolean isDryRun() {
        if (unsafeConfig()) {
            return true;
        }else
            return dryRun;
    }

    private boolean unsafeConfig() {
        boolean shortPrefix = awsCleanupPrefix == null || (awsCleanupPrefix.length() < MIN_PREFIX_LENGTH);
        if (shortPrefix) {
            log.debug("Enforcing dry run, prefix too short {}", awsCleanupPrefix);
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

    public void userWait() {
        try {
            log.info("Are you sure? [ctrl-c to abort]");
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Region getRegion() {
        return Region.of(awsRegion);
    }

    public Tasks getTasks() {
        return tasks;
    }


    public void waitBeforeRun() {
        int millis = 1000;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
