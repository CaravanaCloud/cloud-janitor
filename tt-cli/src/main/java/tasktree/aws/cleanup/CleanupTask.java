package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.Configuration;
import tasktree.aws.AWSTask;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CleanupTask extends AWSTask {
    static final Logger log = LoggerFactory.getLogger(CleanupTask.class);

    @Inject
    Configuration config;

    @PostConstruct
    public void postConstruct(){
        log.debug("Initialized CleanupTask");
        setConfig(config);
        setRegion(config.getRegion());
    }

    @Override
    public void run() {
        log.info("Cleaning up AWS Resources");
        addTask(new FilterRegions());
    }


}
