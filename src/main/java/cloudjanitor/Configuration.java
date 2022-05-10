package cloudjanitor;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import cloudjanitor.aws.AWSClients;
import cloudjanitor.spi.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

@ApplicationScoped
public class Configuration {

    @Inject
    Logger log;


    @ConfigProperty(name = "cj.task", defaultValue = "marvin")
    String taskName;

    @ConfigProperty(name = "cj.dryRun", defaultValue = "true")
    boolean dryRun;

    @ConfigProperty(name = "cj.waitBeforeRun", defaultValue = "1000")
    long waitBeforeRun;

    String[] args;

    @Inject
    AWSClients aws;


    public String getTaskName() {
        return taskName;
    }


    @Override
    public String toString() {
        var dump = new HashMap<String, String>() {{
            put("task", taskName);
            put("args", String.join(" ", args));
            put("dryRun", "" + dryRun);
        }};
        return dump.toString();
    }

    public void parse(String[] args) {
        this.args = args;
        if (args.length > 0) {
            taskName = args[0];
        }
    }


    public void init(String[] args) {
        parse(args);
        log.info("Configuration: {}", this);
        if(!isDryRun()){
            await(3000);
        }
    }

    private void await(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean isDryRun() {
        return dryRun;
    }


    public AWSClients aws(){
        return aws;
    }

    String executionId;

    public synchronized String getExecutionId() {
        if (executionId == null){
            var sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            executionId = "cj-"+sdf.format(new Date());
        }
        return executionId;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
}
