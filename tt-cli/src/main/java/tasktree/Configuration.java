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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;

@ApplicationScoped
public class Configuration {
    @Inject
    Logger log;

    @ConfigProperty(name = "tt.root", defaultValue = "help")
    String root;

    @ConfigProperty(name = "tt.dryRun", defaultValue = "true")
    boolean dryRun;

    @ConfigProperty(name = "tt.aws.region")
    String awsRegion;

    @ConfigProperty(name = "tt.aws.cleanup.prefix")
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
            return writer .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "Configuration{?}";
    }

    public void parse(String[] args) {
        log.info("Args: %s".formatted(Arrays.toString(args)) );
        if (args.length > 0) {
            root = args[0];
        }
    }

    public boolean isDryRun() {
        return dryRun;
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
}
