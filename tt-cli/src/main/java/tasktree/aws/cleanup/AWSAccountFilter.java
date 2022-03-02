package tasktree.aws.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.Configuration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

@Named("cleanup-aws")
@Dependent
public class AWSAccountFilter extends AWSFilter<Void> {
    @Inject
    Logger log;

    @Inject
    Configuration config;

    @Inject
    FilterRegions filterRegions;

    @Inject
    FilterRecords filterRecords;

    @PostConstruct
    public void postConstruct(){
        log.debug("Initializing Configuration");
        setConfig(config);
        setRegion(config.getRegion());
    }

    @Override
    public void run() {
        log.info("Filtering AWS Resources");
        addAllTasks(filterRegions,
                filterRecords);
    }

    @Override
    public String toString() {
        return toString("AWS Account");
    }
}
